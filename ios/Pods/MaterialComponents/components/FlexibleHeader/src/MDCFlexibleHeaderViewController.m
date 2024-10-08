// Copyright 2015-present the Material Components for iOS authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#import "MDCFlexibleHeaderViewController.h"

#import "private/MDCFlexibleHeaderHairline.h"
#import "private/MDCFlexibleHeaderView+Private.h"
#import "MDCAvailability.h"
#import "MDCFlexibleHeaderContainerViewController.h"
#import "MDCFlexibleHeaderSafeAreaDelegate.h"
#import "MDCFlexibleHeaderView+ShiftBehavior.h"
#import "MDCFlexibleHeaderView.h"
#import "MDCFlexibleHeaderViewDelegate.h"
#import "MDCFlexibleHeaderViewLayoutDelegate.h"
#import "MDCFlexibleHeaderShiftBehaviorEnabledWithStatusBar.h"
#import "UIApplication+MDCAppExtensions.h"
#import "MDCLayoutMetrics.h"
#import <MDFTextAccessibility/MDFTextAccessibility.h>

#if defined(TARGET_OS_VISION) && TARGET_OS_VISION
// For code review, use the review queue listed in go/material-visionos-review.
#define IS_VISIONOS 1
#else
#define IS_VISIONOS 0
#endif

@interface UIView ()
- (UIEdgeInsets)safeAreaInsets;  // For pre-iOS 11 SDK targets.
@end

static inline UIStatusBarStyle StatusBarStyleOnBackgroundColor(UIColor *color) {
  if (CGColorGetAlpha(color.CGColor) < 1) {
    return UIStatusBarStyleDefault;
  }

  // We assume that the light iOS status text is white and not big enough to be considered "large"
  // text according to the W3CAG 2.0 spec.
  if ([MDFTextAccessibility textColor:[UIColor whiteColor]
              passesOnBackgroundColor:color
                              options:MDFTextAccessibilityOptionsNone]) {
    return UIStatusBarStyleLightContent;
  }

#if MDC_AVAILABLE_SDK_IOS(13_0)
  if (@available(iOS 13.0, *)) {
    return UIStatusBarStyleDarkContent;
  }
#endif  // MDC_AVAILABLE_SDK_IOS(13_0)

  return UIStatusBarStyleDefault;
}

// KVO contexts
static char *const kKVOContextMDCFlexibleHeaderViewController =
    "kKVOContextMDCFlexibleHeaderViewController";

@interface MDCFlexibleHeaderViewController () <MDCFlexibleHeaderViewDelegate>

/*
 The current height offset of the flexible header controller with the addition of the current status
 bar state at any given time.

 This property is used to determine the bottom point of the |flexibleHeaderView| within the window.
 */
@property(nonatomic) CGFloat flexibleHeaderViewControllerHeightOffset;

/*
 This is the target top layout guide that we will modify such that it includes the flexible header's
 height and any top safe area insets we were able to infer.

 We hold a strong reference to it because on pre-iOS 11 devices UIKit will attempt to reset the
 top layout guide. We observe (using KVO) any changes made to the constraint and reset the
 constraint's length when a modification outside of our awareness occurs.
*/
@property(nonatomic, strong) NSLayoutConstraint *topLayoutGuideConstraint;

/*
 For avoiding re-entrant recursion while modifying the top layout guide.
 */
@property(nonatomic) BOOL isUpdatingTopLayoutGuide;

/*
 On pre-iOS 11 devices, we use this layout constraint to extract the top safe area inset from the
 root ancestor view controller.
 */
@property(nonatomic, strong) NSLayoutConstraint *topSafeAreaConstraint;

/*
 On iOS 11+ devices, we use this view to extract the top safe area inset from the root ancestor view
 controller.
 */
@property(nonatomic, strong) UIView *topSafeAreaView;

/**
 Supports the behavior of showing a narrow line at the bottom edge of the flexible header view.
 */
@property(nonatomic, strong) MDCFlexibleHeaderHairline *hairline;

@property(nonatomic, getter=isTopLayoutGuideAdjustmentEnabled) BOOL topLayoutGuideAdjustmentEnabled;
@property(nonatomic)
    BOOL permitInferringTopSafeAreaFromTopLayoutGuideViewController NS_AVAILABLE_IOS(11.0);

@end

@implementation MDCFlexibleHeaderViewController

@synthesize preferredStatusBarStyle = _preferredStatusBarStyle;

- (void)dealloc {
  // Clear KVO observers
  self.topLayoutGuideConstraint = nil;
  self.topSafeAreaConstraint = nil;
  self.topSafeAreaView = nil;
}

- (instancetype)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    [self commonMDCFlexibleHeaderViewControllerInit];
  }
  return self;
}

- (instancetype)initWithCoder:(NSCoder *)aDecoder {
  self = [super initWithCoder:aDecoder];
  if (self) {
    [self commonMDCFlexibleHeaderViewControllerInit];
  }
  return self;
}

- (void)commonMDCFlexibleHeaderViewControllerInit {
  _inferPreferredStatusBarStyle = YES;

#if IS_VISIONOS
  UIWindow *keyWindow = nil;
  for (UIScene *scene in UIApplication.sharedApplication.connectedScenes) {
    if ([scene isKindOfClass:[UIWindowScene class]]) {
      UIWindowScene *windowScene = (UIWindowScene *)scene;
      keyWindow = windowScene.keyWindow;
      break;
    }
  }
  MDCFlexibleHeaderView *headerView =
      [[MDCFlexibleHeaderView alloc] initWithFrame:keyWindow.bounds];
#else
  MDCFlexibleHeaderView *headerView =
      [[MDCFlexibleHeaderView alloc] initWithFrame:[UIScreen mainScreen].bounds];
#endif
  headerView.autoresizingMask = UIViewAutoresizingFlexibleWidth;
  headerView.delegate = self;
  _headerView = headerView;

  self.hairline = [[MDCFlexibleHeaderHairline alloc] initWithContainerView:_headerView];
}

- (void)loadView {
  self.view = self.headerView;
}

- (void)willMoveToParentViewController:(UIViewController *)parent {
  [super willMoveToParentViewController:parent];

#if !IS_VISIONOS
  BOOL shouldDisableAutomaticInsetting = YES;
  // Prior to iOS 11 there was no way to know whether UIKit had injected insets into our
  // UIScrollView, so we disable automatic insetting on these devices. iOS 11 provides
  // the adjustedContentInset API which allows us to respond to changes in the safe area
  // insets, so on iOS 11 we actually expect iOS to manage the safe area insets.
  shouldDisableAutomaticInsetting = NO;
  if (shouldDisableAutomaticInsetting) {
    parent.automaticallyAdjustsScrollViewInsets = NO;
  }
#endif
}

- (void)didMoveToParentViewController:(UIViewController *)parent {
  [super didMoveToParentViewController:parent];

#if TARGET_OS_UIKITFORMAC
  // When running under UIKit for Mac, the window size may not match the value returned by
  // `[UIScreen mainScreen].bounds` when `commonMDCFlexibleHeaderViewControllerInit` was
  // called, so the width will be updated here to match the hosting view.
  CGRect headerFrame = _headerView.frame;
  headerFrame.size.width = CGRectGetWidth(_headerView.superview.bounds);
  _headerView.frame = headerFrame;
#endif

  // The header depends on the tracking scroll view to know how tall it should be.
  // If there is no tracking scroll view then we have to poke the header into sizing itself.
  if (!_headerView.trackingScrollView) {
    [_headerView sizeToFit];
  } else if (!_headerView.observesTrackingScrollViewScrollEvents) {
    [_headerView trackingScrollViewDidScroll];
  }

  if (self.topLayoutGuideAdjustmentEnabled) {
    [self updateTopLayoutGuide];
  }
}

- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];

  if (self.inferTopSafeAreaInsetFromViewController) {
    // At this point we can be confident that our view controller ancestry is as complete as
    // possible, so we can now infer our top safe area source view controller.
    [self fhv_inferTopSafeAreaSourceViewController];

    // Querying the top layout guide ensures that the flexible header receives layout event when
    // the status bar visibility changes. This allows the flexible header to animate alongside any
    // status bar visibility changes.
#if !IS_VISIONOS
    [self.parentViewController topLayoutGuide];
#else
    [self.parentViewController.view safeAreaLayoutGuide];
#endif
  }

  if (self.topLayoutGuideAdjustmentEnabled) {
    [self updateTopLayoutGuide];

  } else {
#if !IS_VISIONOS
    // Legacy behavior.
    for (NSLayoutConstraint *constraint in self.parentViewController.view.constraints) {
      // Because topLayoutGuide is a readonly property on a viewController we must manipulate
      // the present one via the NSLayoutConstraint attached to it. Thus we keep reference to it.
      if (constraint.firstItem == self.parentViewController.topLayoutGuide &&
          constraint.secondItem == nil) {
        self.topLayoutGuideConstraint = constraint;
      }
    }
#endif

    // On moving to parentViewController, we calculate the height
    self.flexibleHeaderViewControllerHeightOffset = [self headerViewControllerHeight];
  }

#if DEBUG
  NSAssert(![self.parentViewController.parentViewController
               isKindOfClass:[MDCFlexibleHeaderContainerViewController class]],
           @"An instance of %@ has been injected into a view controller (%@) that is already"
           @" wrapped by an instance of %@ - this is not allowed and will cause double headers to"
           @" appear. Choose to either wrap or inject your view controller (preferring injection"
           @" where possible).",
           NSStringFromClass([self class]), NSStringFromClass([self.parentViewController class]),
           NSStringFromClass([MDCFlexibleHeaderContainerViewController class]));
#endif
}

- (UIStatusBarStyle)preferredStatusBarStyle {
  if (self.inferPreferredStatusBarStyle) {
    UIColor *backgroundColor =
        [MDCFlexibleHeaderView appearance].backgroundColor ?: _headerView.backgroundColor;
    return StatusBarStyleOnBackgroundColor(backgroundColor);
  } else {
    return _preferredStatusBarStyle;
  }
}

- (void)setPreferredStatusBarStyle:(UIStatusBarStyle)preferredStatusBarStyle {
  NSAssert(!self.inferPreferredStatusBarStyle,
           @"You must disable inferPreferredStatusBarStyle prior to setting a status bar style.");

  _preferredStatusBarStyle = preferredStatusBarStyle;
}

- (BOOL)prefersStatusBarHidden {
  return _headerView.prefersStatusBarHidden;
}

- (void)viewWillTransitionToSize:(CGSize)size
       withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator {
  [super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];

  [_headerView viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
}

- (void)viewWillLayoutSubviews {
  [super viewWillLayoutSubviews];

  if (self.topLayoutGuideAdjustmentEnabled) {
    [self updateTopLayoutGuide];
  }
}

#pragma mark TraitCollection

- (void)traitCollectionDidChange:(UITraitCollection *)previousTraitCollection {
  [super traitCollectionDidChange:previousTraitCollection];

  if (self.traitCollectionDidChangeBlock) {
    self.traitCollectionDidChangeBlock(self, previousTraitCollection);
  }
}

#pragma mark - UIScrollViewDelegate

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
  if (scrollView == _headerView.trackingScrollView) {
    [_headerView trackingScrollViewDidScroll];
  }
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
  if (scrollView == _headerView.trackingScrollView) {
    [_headerView trackingScrollViewDidEndDecelerating];
  }
}

- (void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate {
  if (scrollView == _headerView.trackingScrollView) {
    [_headerView trackingScrollViewDidEndDraggingWillDecelerate:decelerate];
  }
}

- (void)scrollViewWillEndDragging:(UIScrollView *)scrollView
                     withVelocity:(CGPoint)velocity
              targetContentOffset:(inout CGPoint *)targetContentOffset {
  if (scrollView == _headerView.trackingScrollView) {
    [_headerView trackingScrollViewWillEndDraggingWithVelocity:velocity
                                           targetContentOffset:targetContentOffset];
  }
}

#pragma mark KVO

- (void)observeValueForKeyPath:(NSString *)keyPath
                      ofObject:(id)object
                        change:(NSDictionary *)change
                       context:(void *)context {
  if (context == kKVOContextMDCFlexibleHeaderViewController) {
    void (^mainThreadWork)(void) = ^{
      if (object == self->_topLayoutGuideConstraint && self.topLayoutGuideAdjustmentEnabled) {
        [self updateTopLayoutGuide];
      }
      if (self.inferTopSafeAreaInsetFromViewController &&
          (object == self->_topSafeAreaConstraint || object == self->_topSafeAreaView)) {
        [self->_headerView topSafeAreaInsetDidChange];
      }
    };

    // Ensure that UIKit modifications occur on the main thread.
    if ([NSThread isMainThread]) {
      mainThreadWork();
    } else {
      [[NSOperationQueue mainQueue] addOperationWithBlock:mainThreadWork];
    }

  } else {
    [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
  }
}

#pragma mark - Top layout guide support

/*
 When the flexible header's height changes, we want to adjust the topLayoutGuide length of the
 content view controller so that its content can adjust accordingly. This is the same behavior that
 UIKit container view controllers provide.

 Unfortunately, topLayoutGuide is a read-only property on UIViewController with no way to
 override it, and no public setter for the length.

 The only known way to modify this property programmatically is to access the view controller's
 view constraints and extract the first constraint that contains the top layout guide (and only
 the top layout guide). Modifying the "constant" property of this constraint has the
 undocumented side effect of also updating the topLayoutGuide's length.
 This approach is discussed here:
 https://stackoverflow.com/questions/19588171/how-to-set-toplayoutguide-position-for-child-view-controller

 Also see this open radar feature request for a mutable top layout guide:
 http://www.openradar.me/19984939
 */
- (void)fhv_extractTopLayoutGuideConstraint {
  UIViewController *topLayoutGuideViewController =
      [self fhv_topLayoutGuideViewControllerWithFallback];
  if (!topLayoutGuideViewController.isViewLoaded) {
    self.topLayoutGuideConstraint = nil;
    return;
  }
  if (self.topLayoutGuideAdjustmentEnabled ||
      [topLayoutGuideViewController.view.constraints count] > 0) {
    self.topLayoutGuideConstraint =
        [self fhv_topLayoutGuideConstraintForViewController:topLayoutGuideViewController];
  }
}

- (NSLayoutConstraint *)fhv_topLayoutGuideConstraintForViewController:
    (UIViewController *)viewController {
#if IS_VISIONOS
  // We could look at the view's safe area anchors if this is incorrect.
  return nil;
#else
  // Note: accessing topLayoutGuide has the side effect of setting up all of the view controller
  // constraints. We need to access this property before we enter the for loop, otherwise
  // view.constraints will be empty.
  id<UILayoutSupport> topLayoutGuide = viewController.topLayoutGuide;
  NSLayoutConstraint *foundConstraint = nil;
  for (NSLayoutConstraint *constraint in viewController.view.constraints) {
    if (constraint.firstItem == topLayoutGuide && constraint.secondItem == nil) {
      foundConstraint = constraint;
    }
  }
  return foundConstraint;
#endif
}

- (void)setTopLayoutGuideConstraint:(NSLayoutConstraint *)topLayoutGuideConstraint {
  if (_topLayoutGuideConstraint == topLayoutGuideConstraint) {
    return;
  }

  /*
   On pre-iOS 11 devices, the top layout guide's constant gets set by UIKit multiple times on
   call stacks that we have no influence over, meaning it's easy for our custom top layout guide
   value to be lost (being reset to UIKit's default of "20" usually). We want to have final say on
   the value though, so we KVO the property and re-apply our calculated top layout guide any time
   the top layout guide is modified.

   We only do this on pre-iOS 11 devices because iOS 11 and above are less aggressive about setting
   the top layout guide constant (and clients should be relying on additionalSafeAreaInsets anyway).
   */
  BOOL shouldObserveLayoutGuideConstant = YES;
  shouldObserveLayoutGuideConstant = NO;

  if (shouldObserveLayoutGuideConstant) {
    [_topLayoutGuideConstraint removeObserver:self
                                   forKeyPath:NSStringFromSelector(@selector(constant))
                                      context:kKVOContextMDCFlexibleHeaderViewController];
  }

  _topLayoutGuideConstraint = topLayoutGuideConstraint;

  if (shouldObserveLayoutGuideConstant) {
    [_topLayoutGuideConstraint addObserver:self
                                forKeyPath:NSStringFromSelector(@selector(constant))
                                   options:NSKeyValueObservingOptionNew
                                   context:kKVOContextMDCFlexibleHeaderViewController];
  }
}

- (void)setTopSafeAreaConstraint:(NSLayoutConstraint *)topSafeAreaConstraint {
  if (_topSafeAreaConstraint == topSafeAreaConstraint) {
    return;
  }

  [_topSafeAreaConstraint removeObserver:self
                              forKeyPath:NSStringFromSelector(@selector(constant))
                                 context:kKVOContextMDCFlexibleHeaderViewController];

  _topSafeAreaConstraint = topSafeAreaConstraint;

  [_topSafeAreaConstraint addObserver:self
                           forKeyPath:NSStringFromSelector(@selector(constant))
                              options:NSKeyValueObservingOptionNew
                              context:kKVOContextMDCFlexibleHeaderViewController];
}

- (void)setTopSafeAreaView:(UIView *)topSafeAreaView {
  if (_topSafeAreaView == topSafeAreaView) {
    return;
  }

  [_topSafeAreaView removeObserver:self
                        forKeyPath:NSStringFromSelector(@selector(safeAreaInsets))
                           context:kKVOContextMDCFlexibleHeaderViewController];

  _topSafeAreaView = topSafeAreaView;

  [_topSafeAreaView addObserver:self
                     forKeyPath:NSStringFromSelector(@selector(safeAreaInsets))
                        options:NSKeyValueObservingOptionNew
                        context:kKVOContextMDCFlexibleHeaderViewController];
}

- (UIViewController *)fhv_topLayoutGuideViewControllerWithFallback {
  UIViewController *topLayoutGuideViewController = self.topLayoutGuideViewController;
  if (!topLayoutGuideViewController && !self.topLayoutGuideAdjustmentEnabled) {
    topLayoutGuideViewController = self.parentViewController;
  }
  return topLayoutGuideViewController;
}

- (void)fhv_setTopLayoutGuideConstraintConstant:(CGFloat)constant {
  self.isUpdatingTopLayoutGuide = YES;
  self.topLayoutGuideConstraint.constant = constant;
  self.isUpdatingTopLayoutGuide = NO;
}

- (void)updateTopLayoutGuide {
  NSAssert([NSThread isMainThread], @"updateTopLayoutGuide must be called from the main thread.");
  NSAssert(!self.useAdditionalSafeAreaInsetsForWebKitScrollViews ||
               (self.topLayoutGuideViewController != nil),
           @"If useAdditionalSafeAreaInsetsForWebKitScrollViews is enabled you must also set a"
           @"topLayoutGuideViewController.");
  // We observe (using KVO) the top layout guide's constant and re-invoke updateTopLayoutGuide
  // whenever it changes. We also change the constant in this method. So, to avoid a recursive
  // infinite loop we bail out early here if we're the ones that initiated the top layout guide
  // constant change.
  if (self.isUpdatingTopLayoutGuide) {
    return;
  }

  if (!self.topLayoutGuideAdjustmentEnabled) {
    // Legacy behavior
    [self fhv_setTopLayoutGuideConstraintConstant:self.flexibleHeaderViewControllerHeightOffset];
    return;
  }

  if (![self isViewLoaded]) {
    return;
  }
  if (!self.topLayoutGuideConstraint) {
    [self fhv_extractTopLayoutGuideConstraint];
  }
  CGFloat topInset = CGRectGetMaxY(_headerView.frame);
  // Avoid excessive re-calculations.
  if (self.topLayoutGuideConstraint.constant != topInset) {
    [self fhv_setTopLayoutGuideConstraintConstant:topInset];
  }

  BOOL alwaysUseAdditionalSafeAreaInsets = NO;
  if (self.useAdditionalSafeAreaInsetsForWebKitScrollViews &&
      [self.headerView trackingScrollViewIsWebKit]) {
    alwaysUseAdditionalSafeAreaInsets = YES;
  }

  UIViewController *topLayoutGuideViewController =
      [self fhv_topLayoutGuideViewControllerWithFallback];
  // If there is a tracking scroll view then the flexible header will manage safe area insets via
  // the tracking scroll view's contentInsets. Some day - in the long distant future when we only
  // support iOS 11 and up - we can probably drop the content inset adjustment behavior in favor
  // of modifying additionalSafeAreaInsets instead.
  if (self.headerView.trackingScrollView != nil && !alwaysUseAdditionalSafeAreaInsets) {
    // Reset the additional safe area insets if we are now tracking a scroll view.
    if (topLayoutGuideViewController != nil) {
      UIEdgeInsets additionalSafeAreaInsets = topLayoutGuideViewController.additionalSafeAreaInsets;
      additionalSafeAreaInsets.top = 0;
      topLayoutGuideViewController.additionalSafeAreaInsets = additionalSafeAreaInsets;
    }

  } else if (topLayoutGuideViewController != nil) {
    UIEdgeInsets additionalSafeAreaInsets = topLayoutGuideViewController.additionalSafeAreaInsets;
    // We need to avoid double-counting any top safe area amount because this will already be
    // taken into account as part of additionalSafeAreaInsets.
    topInset -= self.headerView.topSafeAreaGuideHeight;
    if (self.headerView.minMaxHeightIncludesSafeArea) {
      topInset = MIN(self.headerView.maximumHeight - MDCDeviceTopSafeAreaInset(), topInset);
    } else {
      topInset = MIN(self.headerView.maximumHeight, topInset);
    }
    additionalSafeAreaInsets.top = topInset;
    topLayoutGuideViewController.additionalSafeAreaInsets = additionalSafeAreaInsets;
  }
}

- (CGFloat)headerViewControllerHeight {
  BOOL shiftEnabledForStatusBar =
      _headerView.shiftBehavior == MDCFlexibleHeaderShiftBehaviorEnabledWithStatusBar;
#if IS_VISIONOS
  CGFloat statusBarHeight = 0.0;
#else
  CGFloat statusBarHeight = [UIApplication mdc_safeSharedApplication].statusBarFrame.size.height;
#endif
  CGFloat height = MAX(_headerView.frame.origin.y + _headerView.frame.size.height,
                       shiftEnabledForStatusBar ? 0 : statusBarHeight);
  return height;
}

- (void)setTopLayoutGuideViewController:(UIViewController *)topLayoutGuideViewController {
  if (topLayoutGuideViewController == _topLayoutGuideViewController) {
    return;
  }
  _topLayoutGuideViewController = topLayoutGuideViewController;
  _topLayoutGuideAdjustmentEnabled = YES;

  if (self.inferTopSafeAreaInsetFromViewController) {
    // Need to re-infer the top safe area source view controller because it may now be a child of
    // the top layout guide view controller.
    [self fhv_inferTopSafeAreaSourceViewController];
  }

  if ([self isViewLoaded]) {
    [self fhv_extractTopLayoutGuideConstraint];
    [self updateTopLayoutGuide];
  }
}

- (void)setInferTopSafeAreaInsetFromViewController:(BOOL)inferTopSafeAreaInsetFromViewController {
  _headerView.inferTopSafeAreaInsetFromViewController = inferTopSafeAreaInsetFromViewController;

  [self fhv_inferTopSafeAreaSourceViewController];
}

- (BOOL)inferTopSafeAreaInsetFromViewController {
  return _headerView.inferTopSafeAreaInsetFromViewController;
}

- (void)setUseAdditionalSafeAreaInsetsForWebKitScrollViews:
    (BOOL)useAdditionalSafeAreaInsetsForWebKitScrollViews {
  _headerView.useAdditionalSafeAreaInsetsForWebKitScrollViews =
      useAdditionalSafeAreaInsetsForWebKitScrollViews;
}

- (BOOL)useAdditionalSafeAreaInsetsForWebKitScrollViews {
  return _headerView.useAdditionalSafeAreaInsetsForWebKitScrollViews;
}

- (void)setPermitInferringTopSafeAreaFromTopLayoutGuideViewController:
    (BOOL)permitInferringTopSafeAreaFromTopLayoutGuideViewController {
  _permitInferringTopSafeAreaFromTopLayoutGuideViewController =
      permitInferringTopSafeAreaFromTopLayoutGuideViewController;
  [self fhv_inferTopSafeAreaSourceViewController];
}

#pragma mark - Top safe area inset extraction

- (BOOL)fhv_isViewControllerDescendantOfTopLayoutGuideViewController:(UIViewController *)child {
  // No need to walk the ancestry.
  if (self.topLayoutGuideViewController == nil) {
    return NO;
  }

  UIViewController *ancestorIterator = child;
  while (ancestorIterator != nil) {
    if (ancestorIterator == self.topLayoutGuideViewController) {
      return YES;
    }
    ancestorIterator = ancestorIterator.parentViewController;
  }
  return NO;
}

- (UIViewController *)fhv_rootAncestorOfViewController:(UIViewController *)viewController {
  while (viewController.parentViewController != nil) {
    viewController = viewController.parentViewController;
  }
  return viewController;
}

// This method makes the assumption that the root-most view controller is the best view controller
// to look at when determining the top safe area inset. It's possible that this assumption will not
// hold true in all cases, so we may also need to expose an explicit API for setting the top safe
// area source view controller.
- (void)fhv_inferTopSafeAreaSourceViewController {
  UIViewController *parent = self.parentViewController;
  if (parent == nil || !self.inferTopSafeAreaInsetFromViewController) {
    _headerView.topSafeAreaSourceViewController = nil;
    self.topSafeAreaConstraint = nil;
    self.topSafeAreaView = nil;
    return;
  }

  UIViewController *ancestor =
      [self.safeAreaDelegate flexibleHeaderViewControllerTopSafeAreaInsetViewController:self];
  if (ancestor == nil) {
    ancestor = [self fhv_rootAncestorOfViewController:parent];

    // Use instance variable here instead of property, as property is marked as available only on
    // iOS 11+.
    if (_permitInferringTopSafeAreaFromTopLayoutGuideViewController) {
      // On iOS 11, we can subtract the additional safe area insets to find the correct value.
    } else {
      // Are we attempting to extract the top safe area inset from our top layout guide view
      // controller?
      if (self.topLayoutGuideAdjustmentEnabled && ancestor == self.topLayoutGuideViewController) {
        // We can't use the provided ancestor because it's a child of the top layout guide view
        // controller. Doing so would result in the top layout guide being infinitely increased.
        ancestor = nil;
      }
    }
  }

  // if ancestor == nil at this point, then we're in a bad spot because there's nowhere for us to
  // extract a top safe area inset from. Should we throw an assert?
  NSAssert(ancestor != nil,
           @"inferTopSafeAreaInsetFromViewController is true but we were unable to infer a view "
           @"controller from which we could extract a safe area. Consider placing your view "
           @"controller inside a container view controller.");

  if (_headerView.topSafeAreaSourceViewController != ancestor) {
    _headerView.topSafeAreaSourceViewController = ancestor;
    _headerView.subtractsAdditionalSafeAreaInsets =
        // Use instance variable here instead of property, as property is marked as available only
        // on iOS 11+.
        _permitInferringTopSafeAreaFromTopLayoutGuideViewController &&
        self.topLayoutGuideAdjustmentEnabled && ancestor == self.topLayoutGuideViewController;

    BOOL shouldObserveLayoutGuide = YES;
    shouldObserveLayoutGuide = NO;
    if (shouldObserveLayoutGuide) {
      self.topSafeAreaConstraint = [self fhv_topLayoutGuideConstraintForViewController:ancestor];
    } else {
      self.topSafeAreaView = ancestor.view;
    }
  }
}

#pragma mark MDCFlexibleHeaderViewDelegate

- (void)flexibleHeaderViewNeedsStatusBarAppearanceUpdate:
    (__unused MDCFlexibleHeaderView *)headerView {
#if !IS_VISIONOS
  [self setNeedsStatusBarAppearanceUpdate];
#endif
}

- (void)flexibleHeaderViewFrameDidChange:(MDCFlexibleHeaderView *)headerView {
  if (self.topLayoutGuideAdjustmentEnabled) {
    [self updateTopLayoutGuide];

  } else {
    // Legacy behavior
    // Whenever the flexibleHeaderView's frame changes, we update the value of the height offset
    self.flexibleHeaderViewControllerHeightOffset = [self headerViewControllerHeight];

    // We must change the constant of the constraint attached to our parentViewController's
    // topLayoutGuide to trigger the re-layout of its subviews
    [self fhv_setTopLayoutGuideConstraintConstant:self.flexibleHeaderViewControllerHeightOffset];
  }

  [self.layoutDelegate flexibleHeaderViewController:self
                   flexibleHeaderViewFrameDidChange:headerView];
}

#pragma mark - Hairline support

- (void)setShowsHairline:(BOOL)showsHairline {
  self.hairline.hidden = !showsHairline;
}

- (BOOL)showsHairline {
  return !self.hairline.hidden;
}

- (void)setHairlineColor:(UIColor *)hairlineColor {
  self.hairline.color = hairlineColor;
}

- (UIColor *)hairlineColor {
  return self.hairline.color;
}

@end

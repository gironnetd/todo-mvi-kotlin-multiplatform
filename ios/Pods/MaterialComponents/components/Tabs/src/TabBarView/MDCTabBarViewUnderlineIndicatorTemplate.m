// Copyright 2017-present the Material Components for iOS authors. All Rights Reserved.
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

#import "MDCTabBarViewUnderlineIndicatorTemplate.h"

#import "MDCTabBarViewIndicatorAttributes.h"
#import "MDCTabBarViewIndicatorContext.h"

NS_ASSUME_NONNULL_BEGIN

/// Height in points of the underline shown under selected items.
static const CGFloat kUnderlineIndicatorHeight = 2;

@implementation MDCTabBarViewUnderlineIndicatorTemplate

- (MDCTabBarViewIndicatorAttributes *)indicatorAttributesForContext:
    (id<MDCTabBarViewIndicatorContext>)context {
  CGRect bounds = context.bounds;
  MDCTabBarViewIndicatorAttributes *attributes = [[MDCTabBarViewIndicatorAttributes alloc] init];
  CGRect underlineFrame =
      CGRectMake(CGRectGetMinX(bounds), CGRectGetMaxY(bounds) - kUnderlineIndicatorHeight,
                 CGRectGetWidth(bounds), kUnderlineIndicatorHeight);
  attributes.path = [UIBezierPath bezierPathWithRect:underlineFrame];
  return attributes;
}

@end

NS_ASSUME_NONNULL_END

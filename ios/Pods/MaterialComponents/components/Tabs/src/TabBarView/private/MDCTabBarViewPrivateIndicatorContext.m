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


#import "MDCTabBarViewPrivateIndicatorContext.h"

NS_ASSUME_NONNULL_BEGIN

@implementation MDCTabBarViewPrivateIndicatorContext
@synthesize bounds = _bounds;
@synthesize contentFrame = _contentFrame;
@synthesize item = _item;

- (instancetype)initWithItem:(UITabBarItem *)item
                      bounds:(CGRect)bounds
                contentFrame:(CGRect)contentFrame {
  self = [super init];
  if (self) {
    _item = item;
    _bounds = bounds;
    _contentFrame = contentFrame;
  }
  return self;
}

#pragma mark - NSObject

- (NSString *)description {
  return [NSString stringWithFormat:@"%@ item:%@ bounds:%@ frame:%@", [super description], _item,
                                    NSStringFromCGRect(_bounds), NSStringFromCGRect(_contentFrame)];
}

- (BOOL)isEqual:(id)object {
  if (![object isKindOfClass:[self class]]) {
    return NO;
  }

  MDCTabBarViewPrivateIndicatorContext *otherContext = object;

  if ((_item != otherContext.item) && ![_item isEqual:otherContext.item]) {
    return NO;
  }

  if (!CGRectEqualToRect(_bounds, otherContext.bounds)) {
    return NO;
  }

  if (!CGRectEqualToRect(_contentFrame, otherContext.contentFrame)) {
    return NO;
  }

  return YES;
}

- (NSUInteger)hash {
  return _item.hash;
}

@end

NS_ASSUME_NONNULL_END

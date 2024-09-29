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

#import <UIKit/UIKit.h>

API_DEPRECATED_BEGIN("🤖👀 Use a branded MDCTabBarView instead. "
                     "See go/material-ios-tabs and go/material-ios-tabbar-migration for more info. "
                     "This has go/material-ios-migrations#scriptable-potential 🤖👀.",
                     ios(12, 12))

@class MDCTabBarIndicatorAttributes;
@protocol MDCTabBarIndicatorContext;

/*
 Template for indicator content which defines how the indicator changes appearance in response to
 changes in its context.

 Template objects are expected to be immutable once set on a tab bar.
 */
@protocol MDCTabBarIndicatorTemplate <NSObject>

/**
 Returns an attributes object that describes how the indicator should appear in a given context.
 */
- (nonnull MDCTabBarIndicatorAttributes *)indicatorAttributesForContext:
    (nonnull id<MDCTabBarIndicatorContext>)context;

@end

API_DEPRECATED_END

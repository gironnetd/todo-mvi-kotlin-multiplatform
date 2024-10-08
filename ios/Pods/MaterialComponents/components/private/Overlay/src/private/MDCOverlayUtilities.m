// Copyright 2016-present the Material Components for iOS authors. All Rights Reserved.
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

#import "MDCOverlayUtilities.h"

CGRect MDCOverlayConvertRectToView(CGRect screenRect, UIView *target) {
#if !TARGET_OS_VISION
  if (target != nil && !CGRectIsNull(screenRect)) {
    UIScreen *screen = [UIScreen mainScreen];
    return [target convertRect:screenRect fromCoordinateSpace:screen.coordinateSpace];
  }
#endif  // TODO: b/359236816 - fix visionOS-specific compatibility workarounds.
  return CGRectNull;
}

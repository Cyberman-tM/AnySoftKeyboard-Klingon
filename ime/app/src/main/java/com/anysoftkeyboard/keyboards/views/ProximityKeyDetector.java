/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.keyboards.views;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import java.util.Arrays;

class ProximityKeyDetector extends KeyDetector {
  private static final int MAX_NEARBY_KEYS = 36;

  // working area
  private int[] mDistances = new int[MAX_NEARBY_KEYS];

  ProximityKeyDetector() {
    super();
  }

  @Override
  protected int getMaxNearbyKeys() {
    return MAX_NEARBY_KEYS;
  }

  @Override
  public int getKeyIndexAndNearbyCodes(int x, int y, int[] allKeys) {
    final AnyKeyboard keyboard = mKeyboard;
    if (keyboard == null) return 0;

    final Keyboard.Key[] keys = getKeys();
    final int touchX = getTouchX(x);
    final int touchY = getTouchY(y);
    int primaryIndex = AnyKeyboardViewBase.NOT_A_KEY;
    int closestKey = AnyKeyboardViewBase.NOT_A_KEY;
    int closestKeyDist = mProximityThresholdSquare + 1;
    boolean hasSpace = false;
    int[] distances = mDistances;
    Arrays.fill(distances, Integer.MAX_VALUE);
    int[] nearestKeyIndices = keyboard.getNearestKeysIndices(touchX, touchY);
    for (int nearestKeyIndex : nearestKeyIndices) {
      final Keyboard.Key key = keys[nearestKeyIndex];

      int dist = 0;
      boolean isInside = key.isInside(touchX, touchY);
      if (isInside) {
        primaryIndex = nearestKeyIndex;
      }

      if (((mProximityCorrectOn
                  && (dist = key.squaredDistanceFrom(touchX, touchY)) < mProximityThresholdSquare)
              || isInside)
          && key.getPrimaryCode() >= KeyCodes.SPACE) {
        // Find insertion point
        final int nCodes = key.getCodesCount();
        if (dist < closestKeyDist) {
          closestKeyDist = dist;
          closestKey = nearestKeyIndex;
        }

        if (allKeys == null) continue;
        if (key.getPrimaryCode() == KeyCodes.SPACE) {
          hasSpace = true;
        }

        for (int j = 0; j < distances.length; j++) {
          if (distances[j] > dist) {
            // Make space for nCodes mCodes
            System.arraycopy(distances, j, distances, j + nCodes, distances.length - j - nCodes);
            System.arraycopy(allKeys, j, allKeys, j + nCodes, allKeys.length - j - nCodes);
            for (int codeIndex = 0; codeIndex < nCodes; codeIndex++)
              allKeys[j + codeIndex] = key.getCodeAtIndex(codeIndex, keyboard.isShifted());
            Arrays.fill(distances, j, j + nCodes, dist);
            break;
          }
        }
      }
    }
    if (primaryIndex == AnyKeyboardViewBase.NOT_A_KEY) {
      primaryIndex = closestKey;
    }

    if (hasSpace) {
      allKeys[allKeys.length - 1] = KeyCodes.SPACE;
    }
    return primaryIndex;
  }
}

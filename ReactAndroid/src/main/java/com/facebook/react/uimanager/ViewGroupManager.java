/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.react.uimanager;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Class providing children management API for view managers of classes extending ViewGroup.
 */
public abstract class ViewGroupManager <T extends ViewGroup>
    extends BaseViewManager<T, LayoutShadowNode> {

  @Override
  public LayoutShadowNode createShadowNodeInstance() {
    return new LayoutShadowNode();
  }

  @Override
  public Class<? extends LayoutShadowNode> getShadowNodeClass() {
    return LayoutShadowNode.class;
  }

  @Override
  public void updateExtraData(T root, Object extraData) {
  }

  public void addView(T parent, View child, int index) {
    parent.addView(child, index);
    reorderChildrenByZIndex(parent);
  }

  public static void reorderChildrenByZIndex(ViewGroup view) {
    // This gets called in a ReactZIndexView in setIndex with
    // the view.getParent(). The view may not have been added
    // to the window yet though, so the parent might be null
    if (view == null) {
      return;
    }

    // Collect all the children that are ZIndexViews
    ArrayList<ReactZIndexView> zIndexViewsToSort = new ArrayList<ReactZIndexView>();
    for (int i = 0; i < view.getChildCount(); i++) {
      View sibling = view.getChildAt(i);
      if (sibling instanceof ReactZIndexView) {
        zIndexViewsToSort.add((ReactZIndexView)sibling);
      }
    }
    // Sort the views by zindex
    Collections.sort(zIndexViewsToSort, new Comparator<ReactZIndexView>() {
      @Override
      public int compare(ReactZIndexView view1, ReactZIndexView view2) {
        return (int)view1.getZIndex() - (int)view2.getZIndex();
      }
    });
    // Call .bringToFront on the sorted list of views
    for (int i = 0; i < zIndexViewsToSort.size(); i++) {
      View sortedView = (View)zIndexViewsToSort.get(i);
      sortedView.bringToFront();
    }
    view.invalidate();
  }

  public int getChildCount(T parent) {
    return parent.getChildCount();
  }

  public View getChildAt(T parent, int index) {
    return parent.getChildAt(index);
  }

  public void removeViewAt(T parent, int index) {
    parent.removeViewAt(index);
  }

  public void removeAllViews(T parent) {
    for (int i = getChildCount(parent) - 1; i >= 0; i--) {
      removeViewAt(parent, i);
    }
  }

  /**
   * Returns whether this View type needs to handle laying out its own children instead of
   * deferring to the standard css-layout algorithm.
   * Returns true for the layout to *not* be automatically invoked. Instead onLayout will be
   * invoked as normal and it is the View instance's responsibility to properly call layout on its
   * children.
   * Returns false for the default behavior of automatically laying out children without going
   * through the ViewGroup's onLayout method. In that case, onLayout for this View type must *not*
   * call layout on its children.
   */
  public boolean needsCustomLayoutForChildren() {
    return false;
  }

}

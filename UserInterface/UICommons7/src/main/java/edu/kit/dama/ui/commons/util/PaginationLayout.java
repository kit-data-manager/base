/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.ui.commons.util;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.ui.commons.interfaces.IPaginationLayoutCallback;
import java.util.List;

/**
 *
 * @author jejkal
 */
public class PaginationLayout<C> extends VerticalLayout {

  private int currentPage = 0;
  private int overallEntries = 0;
  private int entriesPerPage = 5;
  private IPaginationLayoutCallback callback;
  private String caption = null;
  private Panel page;
  private Resource icon;
  private ThemeResource firstIcon;
  private ThemeResource nextIcon;
  private ThemeResource prevIcon;
  private ThemeResource endIcon;

  /**
   * Default constructor showing 5 entries per page with a height of 30px per
   * entry.
   *
   * @param pCallback The pagination callback.
   */
  public PaginationLayout(IPaginationLayoutCallback pCallback) {
    this(5, 30, pCallback);
  }

  /**
   * Default constructor.
   *
   * @param pEntriesPerPage The default amount of entries per page.
   * @param pEntryHeightInPx The height of each entry in px. This value is used
   * to calculate the overall height. By default it is set to 30px.
   * @param pCallback The pagination callback used to obtain and render the
   * entries.
   */
  public PaginationLayout(int pEntriesPerPage, int pEntryHeightInPx, IPaginationLayoutCallback pCallback) {
    super();
    callback = pCallback;
    // setMargin(true);
    if (pEntriesPerPage < 1) {
      throw new IllegalArgumentException("Entries per page must be more than 0");
    }
    entriesPerPage = pEntriesPerPage;
  }

  /**
   * Set the icon for the 'last'-button.
   *
   * @param pResource The icon resource.
   */
  public void setFirstIcon(ThemeResource pResource) {
    firstIcon = pResource;
  }

  /**
   * Set the icon for the 'next'-button.
   *
   * @param pResource The icon resource.
   */
  public void setNextIcon(ThemeResource pResource) {
    nextIcon = pResource;
  }

  /**
   * Set the icon for the 'prev'-button.
   *
   * @param pResource The icon resource.
   */
  public void setPrevIcon(ThemeResource pResource) {
    prevIcon = pResource;
  }

  /**
   * Set the icon for the 'end'-button.
   *
   * @param pResource The icon resource.
   */
  public void setLastIcon(ThemeResource pResource) {
    endIcon = pResource;
  }

  /**
   * Returns the pagination callback.
   *
   * @return The pagination callback.
   */
  public final IPaginationLayoutCallback<C> getCallback() {
    return callback;
  }

  /**
   * Set the overall number of entries.
   *
   * @param overallEntries The number of all entries.
   */
  public final void setOverallEntries(int overallEntries) {
    this.overallEntries = overallEntries;
  }

  /**
   * Get the overall number of entries.
   *
   * @return The number of all entries.
   */
  public final int getOverallEntries() {
    return overallEntries;
  }

  public final int getEntriesPerPage() {
    return entriesPerPage;
  }

  /**
   * Set the number of entries per page.
   *
   * @param entriesPerPage The number of entries per page.
   */
  public final void setEntriesPerPage(int entriesPerPage) {
    this.entriesPerPage = entriesPerPage;
  }

  @Override
  public final void setCaption(String pCaption) {
    caption = pCaption;
  }

  @Override
  public final void setIcon(Resource pIcon) {
    icon = pIcon;
  }

  /**
   * Reset the view by going back to the first page.
   */
  public final void reset() {
    currentPage = 0;
    update();
  }

  /**
   * Returns the currently visible page number.
   *
   * @return The page number.
   */
  public final int getCurrentPage() {
    return currentPage;
  }

  /**
   * Update the layout. This method is either called internally when scrolling
   * or
   */
  public final void update() {
    //remove all components (old result page and navigation)
    removeAllComponents();

    //add current results
    renderPage();

    //build pagination
    int pages = overallEntries / entriesPerPage;
    if (overallEntries % entriesPerPage > 0) {
      pages++;
    }

    final int overallPages = pages;
    HorizontalLayout navigation = new HorizontalLayout();
    //add "JumpToFirstPage" button
    final NativeButton first = new NativeButton();
    first.setIcon(firstIcon);
    if (firstIcon == null) {
      first.setCaption("<<");
    }
    first.setDescription("First Page");
    first.addClickListener(new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        currentPage = 0;
        update();
      }
    });
    //add "PreviousPage" button
    final NativeButton prev = new NativeButton();
    prev.setIcon(prevIcon);
    if (prevIcon == null) {
      prev.setCaption("<");
    }
    prev.setDescription("Previous Page");
    prev.addClickListener(new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        if (currentPage > 0) {
          currentPage--;
          update();
        }
      }
    });
    //add "NextPage" button
    final NativeButton next = new NativeButton();
    next.setIcon(nextIcon);
    if (nextIcon == null) {
      next.setCaption(">");
    }
    next.setDescription("Next Page");
    next.addClickListener(new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        if (currentPage + 1 < overallPages) {
          currentPage++;
          update();
        }
      }
    });
    //add "JumpToLastPage" button
    final NativeButton last = new NativeButton();
    last.setIcon(endIcon);
    if (endIcon == null) {
      next.setCaption(">>");
    }
    last.setDescription("Last Page");
    last.addClickListener(new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        currentPage = overallPages - 1;
        update();
      }
    });

    //enable/disable buttons depending on the current page
    if (overallPages == 0) {
      first.setEnabled(false);
      prev.setEnabled(false);
      next.setEnabled(false);
      last.setEnabled(false);
    } else {
      first.setEnabled(!(currentPage == 0) || !(overallPages < 2));
      prev.setEnabled(!(currentPage == 0) || !(overallPages < 2));
      next.setEnabled(!(currentPage == overallPages - 1) || !(overallPages < 2));
      last.setEnabled(!(currentPage == overallPages - 1) || !(overallPages < 2));
    }

    //at first, put the page size selection box into the navigation
    final ComboBox entriesPerPageBox = new ComboBox();
    entriesPerPageBox.setItemCaptionPropertyId("name");
    entriesPerPageBox.addContainerProperty("name", String.class, null);
    entriesPerPageBox.addItem(5);
    entriesPerPageBox.getContainerProperty(5, "name").setValue("5 Entries / Page");
    entriesPerPageBox.addItem(10);
    entriesPerPageBox.getContainerProperty(10, "name").setValue("10 Entries / Page");
    entriesPerPageBox.addItem(15);
    entriesPerPageBox.getContainerProperty(15, "name").setValue("15 Entries / Page");
    entriesPerPageBox.addItem(20);
    entriesPerPageBox.getContainerProperty(20, "name").setValue("20 Entries / Page");

    entriesPerPageBox.setValue(entriesPerPage);
    entriesPerPageBox.setNullSelectionAllowed(false);
    entriesPerPageBox.setImmediate(true);

    entriesPerPageBox.addListener(new Property.ValueChangeListener() {
      public void valueChange(ValueChangeEvent event) {
        entriesPerPage = (Integer) entriesPerPageBox.getValue();
        update();
      }
    });

    navigation.addComponent(entriesPerPageBox);

    //filler labels are added to the beginning and to the end to keep the navigation in the middle
    Label leftFiller = new Label();
    leftFiller.setWidth("25px");
    navigation.addComponent(leftFiller);
    navigation.addComponent(first);
    navigation.addComponent(prev);

    //Show max. 10 pages at once for performance and layout reasons.
    //If there are more than 10 pages, "move" the to show 10 pages based on the current page.
    int start = currentPage - 5;
    start = (start < 0) ? 0 : start;
    int end = start + 10;
    end = (end > pages) ? pages : end;

    if (end - start < 10 && pages > 10) {
      start = end - 10;
    }

    if (overallPages == 0) {
      Label noEntryLabel = new Label("<i>No entries</i>", Label.CONTENT_XHTML);
      //noEntryLabel.setWidth("80px");
      noEntryLabel.setSizeUndefined();
      navigation.addComponent(noEntryLabel);
      navigation.setComponentAlignment(noEntryLabel, Alignment.MIDDLE_CENTER);
    }
    //build the actual page entries
    for (int i = start; i < end; i++) {
      if (i == currentPage) {
        //the current page is marked with a special style
        Label pageLink = new Label("<b>" + Integer.toString(i + 1) + "</b>", Label.CONTENT_XHTML);
        pageLink.setStyleName("currentpage");
        pageLink.setWidth("15px");
        navigation.addComponent(pageLink);
        navigation.setComponentAlignment(pageLink, Alignment.MIDDLE_CENTER);
      } else {
        //otherwise normal links are added, click-events are handled via LayoutClickListener 
        Link pageLink = new Link(Integer.toString(i + 1), null);
        navigation.addComponent(pageLink);
        navigation.setComponentAlignment(pageLink, Alignment.MIDDLE_CENTER);
      }
    }
    //add right navigation buttons
    navigation.addComponent(next);
    navigation.addComponent(last);
    //...and fill the remaining space 
    Label rightFiller = new Label();
    navigation.addComponent(rightFiller);
    //  navigation.setExpandRatio(leftFiller, 1.0f);
    navigation.setExpandRatio(rightFiller, 1.0f);
    navigation.setSpacing(true);

    //put everything ot the middle
    navigation.setComponentAlignment(first, Alignment.MIDDLE_CENTER);
    navigation.setComponentAlignment(prev, Alignment.MIDDLE_CENTER);
    navigation.setComponentAlignment(next, Alignment.MIDDLE_CENTER);
    navigation.setComponentAlignment(last, Alignment.MIDDLE_CENTER);

    //add layout click listener to be able to navigate by clicking the single pages
    navigation.addListener(new LayoutEvents.LayoutClickListener() {
      @Override
      public void layoutClick(LayoutEvents.LayoutClickEvent event) {

        // Get the child component which was clicked
        Component child = event.getChildComponent();

        if (child == null) {
          // Not over any child component
        } else {
          // Over a child component
          if (child instanceof Link) {
            // Over a valid child element
            currentPage = Integer.parseInt(((Link) child).getCaption()) - 1;
            update();
          }
        }
      }
    });

    //finalize
    navigation.setWidth("100%");
    navigation.setHeight("25px");

    //add navigation and align it right below the result page
    addComponent(page);
    setExpandRatio(page, 1f);
    if (overallEntries > 0) {
      addComponent(navigation);
      setComponentAlignment(navigation, Alignment.BOTTOM_CENTER);
      setExpandRatio(navigation, .05f);
    }
    requestRepaint();
  }

  /**
   * Fill the pagination listing based on a specific query to obtain valid
   * objects.
   */
  private void renderPage() {
    //obtain the objects of this page (5 entries per page are shown)
    int start = currentPage * entriesPerPage;
    //initialize page panel and layout
    page = new Panel();
    page.setCaption(caption);
    page.setImmediate(true);
    page.setIcon(icon);
    VerticalLayout pageLayout = new VerticalLayout();
    pageLayout.setMargin(true);
    pageLayout.setImmediate(true);
    pageLayout.setSizeUndefined();
    page.setSizeFull();
    List<C> entries;

    if (overallEntries > 0) {
      AbstractComponent header = callback.renderHeader();
      if (header != null) {
        pageLayout.addComponent(header);
        pageLayout.setComponentAlignment(header, Alignment.TOP_LEFT);
      }
      entries = callback.getEntries(this, start);
      //add all objects of this page
      int objectIdx = 1;
      for (C entry : entries) {
        AbstractComponent renderedEntry = callback.renderEntry(entry, start + objectIdx);

        pageLayout.addComponent(renderedEntry);
        pageLayout.setComponentAlignment(renderedEntry, Alignment.TOP_CENTER);
        Label spacer = new Label("<hr/>", Label.CONTENT_XHTML);
        spacer.setHeight("3px");
        spacer.setWidth("100%");
        pageLayout.addComponent(spacer);
        pageLayout.setComponentAlignment(spacer, Alignment.TOP_CENTER);
        objectIdx++;
      }

      //if there are less than 'entriesPerPage' entries, add a 'filler' to keep the actual items on top of the layout
      if (objectIdx < entriesPerPage) {
        Label filler = new Label();
        pageLayout.addComponent(filler);
        pageLayout.setExpandRatio(filler, 1.0f);
      }
    } else {
      //nothing visible
      Label filler = new Label("No entries available");
      pageLayout.addComponent(filler);
      pageLayout.setExpandRatio(filler, 1.0f);
    }

    page.setContent(pageLayout);
  }
}

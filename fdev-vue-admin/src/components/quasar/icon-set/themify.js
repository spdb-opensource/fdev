// there are some icons that are needed but not available
// so we import them from MDI as svgs

import {
  mdiFormatBold,
  mdiFormatStrikethroughVariant,
  mdiFormatSubscript,
  mdiFormatSuperscript,
  mdiFormatClear,
  mdiFormatHeader1,
  mdiFormatHeader2,
  mdiFormatHeader3,
  mdiFormatHeader4,
  mdiFormatHeader5,
  mdiFormatHeader6,
  mdiCodeTags,
  mdiNumeric1Box,
  mdiNumeric2Box,
  mdiNumeric3Box,
  mdiNumeric4Box,
  mdiNumeric5Box,
  mdiNumeric6Box,
  mdiNumeric7Box,
  mdiFormatFont
} from '@quasar/extras/mdi-v5';

export default {
  name: 'themify',
  type: {
    positive: 'ti-check',
    negative: 'ti-alert',
    info: 'ti-info-alt',
    warning: 'ti-alert'
  },
  arrow: {
    up: 'ti-arrow-up',
    right: 'ti-arrow-right',
    down: 'ti-arrow-down',
    left: 'ti-arrow-left',
    dropdown: 'ti-arrow-circle-down'
  },
  chevron: {
    left: 'ti-angle-left',
    right: 'ti-angle-right'
  },
  colorPicker: {
    spectrum: 'ti-brush-alt',
    tune: 'ti-panel',
    palette: 'ti-palette'
  },
  pullToRefresh: {
    icon: 'ti-reload'
  },
  carousel: {
    left: 'ti-angle-left',
    right: 'ti-angle-right',
    up: 'ti-angle-up',
    down: 'ti-angle-down',
    navigationIcon: 'ti-control-record',
    thumbnails: 'ti-layout-slider-alt'
  },
  chip: {
    remove: 'ti-close',
    selected: 'ti-check'
  },
  datetime: {
    arrowLeft: 'ti-angle-left',
    arrowRight: 'ti-angle-right',
    now: 'ti-time',
    today: 'ti-calendar'
  },
  editor: {
    italic: 'ti-Italic',
    underline: 'ti-underline',
    unorderedList: 'ti-list',
    orderedList: 'ti-list-ol',
    hyperlink: 'ti-link',
    toggleFullscreen: 'ti-fullscreen',
    quote: 'ti-quote-right',
    left: 'ti-align-left',
    center: 'ti-align-center',
    right: 'ti-align-right',
    justify: 'ti-align-justify',
    print: 'ti-printer',
    outdent: 'ti-angle-double-left',
    indent: 'ti-angle-double-right',
    formatting: 'ti-text',
    fontSize: 'ti-smallcap',
    align: 'ti-align-left',
    hr: 'ti-minus',
    undo: 'ti-share-alt',
    redo: 'ti-share',
    heading: 'ti-paragraph',
    size: 'ti-smallcap',
    viewSource: 'ti-shortcode',

    bold: mdiFormatBold,
    strikethrough: mdiFormatStrikethroughVariant,
    subscript: mdiFormatSubscript,
    superscript: mdiFormatSuperscript,
    removeFormat: mdiFormatClear,
    heading1: mdiFormatHeader1,
    heading2: mdiFormatHeader2,
    heading3: mdiFormatHeader3,
    heading4: mdiFormatHeader4,
    heading5: mdiFormatHeader5,
    heading6: mdiFormatHeader6,
    code: mdiCodeTags,
    size1: mdiNumeric1Box,
    size2: mdiNumeric2Box,
    size3: mdiNumeric3Box,
    size4: mdiNumeric4Box,
    size5: mdiNumeric5Box,
    size6: mdiNumeric6Box,
    size7: mdiNumeric7Box,
    font: mdiFormatFont
  },
  expansionItem: {
    icon: 'ti-angle-down',
    denseIcon: 'ti-arrow-circle-down'
  },
  fab: {
    icon: 'ti-plus',
    activeIcon: 'ti-close'
  },
  field: {
    clear: 'ti-close',
    error: 'ti-alert'
  },
  pagination: {
    first: 'ti-angle-double-left',
    prev: 'ti-angle-left',
    next: 'ti-angle-right',
    last: 'ti-angle-double-right'
  },
  rating: {
    icon: 'ti-star'
  },
  stepper: {
    done: 'ti-check',
    active: 'ti-pencil',
    error: 'ti-alert'
  },
  tabs: {
    left: 'ti-angle-left',
    right: 'ti-angle-right',
    up: 'ti-angle-up',
    down: 'ti-angle-down'
  },
  table: {
    arrowUp: 'ti-arrow-up',
    warning: 'ti-alert',
    firstPage: 'ti-angle-double-left',
    prevPage: 'ti-angle-left',
    nextPage: 'ti-angle-right',
    lastPage: 'ti-angle-double-right'
  },
  tree: {
    icon: 'ti-control-play'
  },
  uploader: {
    done: 'ti-check',
    clear: 'ti-close',
    add: 'ti-support',
    upload: 'ti-cloud-up',
    removeQueue: 'ti-layout-media-right',
    removeUploaded: 'ti-layout-placeholder'
  }
};

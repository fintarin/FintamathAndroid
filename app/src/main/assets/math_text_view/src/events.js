/**
 * Currently selected element.
 *
 * @type {HTMLSpanElement?}
 */
let selectedElem = null;

//---------------------------------------------------------------------------------------------------------//

window.oncontextmenu = onContextMenu;

mathTextView.onmousedown = onMouseDown;

setInterval(function () {
  onSelectedElementChanged();
}, 30);

//---------------------------------------------------------------------------------------------------------//

/**
 * Disables default cotext menu.
 *
 * @param {MouseEvent} event - The context menu event.
 */
function onContextMenu(event) {
  event.preventDefault();
}

/**
 * Disables double click.
 *
 * @param {MouseEvent} event - The mouse down event.
 */
function onMouseDown(event) {
  if (event.detail > 1) {
    event.preventDefault();

    const selection = window.getSelection();
    if (selection.rangeCount === 0) {
      return;
    }

    selection.removeAllRanges();
    selection.addRange(document.caretRangeFromPoint(event.x, event.y));
  }
}

/**
 * Handle a text change in mathTextView.
 */
function onTextChange() {
  insertBordersRec(mathTextView);
  redrawSvg(mathTextView);
  onSelectedElementChanged();

  let mathText = toMathText(mathTextView.innerHTML, mathTextView.isContentEditable);

  if (undoStack.length > 0 && undoStack[undoStack.length - 1][0] === mathText) {
    undoStack.pop();
    return;
  }

  let isMathTextComplete = isComplete(mathTextView) ? 'true' : 'false';

  // Math texts with a single and last character '=' are complete.
  if (mathText.length > 1 && mathText.endsWith('=') && mathText.split('=').length - 1 === 1) {
    mathText = mathText.substring(0, mathText.length - 1);
    isMathTextComplete = 'true';
  }

  Android.onTextChange(mathText, isMathTextComplete);
}

/**
 * Deselects the previously selected element and selects the element where the cursor is currently placed.
 */
function onSelectedElementChanged() {
  deselectElement(selectedElem);

  if (document.activeElement === document.body) {
    return;
  }

  const selection = window.getSelection();
  if (selection.rangeCount === 0) {
    return;
  }

  const range = selection.getRangeAt(0);
  const elem = range.startContainer;

  if (range.collapsed) {
    selectElement(elem);
  }

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Selects the element where the cursor is currently placed and sets its style.
   *
   * @param {HTMLSpanElement} elem - The element where the cursor is currently placed.
   */
  function selectElement(elem) {
    if (!mathTextView.isContentEditable) {
      return;
    }

    if (getClassName(elem) === undefinedClass) {
      elem = elem.parentElement;
    }

    if (elem === selectedElem) {
      return;
    }

    if (getClassName(elem) === mathTextViewClass) {
      return;
    }

    selectedElem = elem;
    selectedElem.setAttribute(emptyHintAttr, textEmptyHintSelected);
    selectedElem.style.textDecoration = textDecorationAttr;

    if (selectedElem.innerHTML !== '') {
      selectedElem.style.textDecorationColor = getColorWithOpacity(mathTextView.style.color, textHintOpacity);
    }
  }

  /**
   * Deselects the element where the cursor is currently placed and sets its style.
   */
  function deselectElement(elem) {
    if (!mathTextView.isContentEditable) {
      return;
    }

    if (elem === null) {
      return;
    }

    if (getClassName(elem) === mathTextViewClass) {
      return;
    }

    elem.setAttribute(emptyHintAttr, '');
    elem.style.textDecoration = textDecorationNoneAttr;

    selectedElem = null;
  }
}

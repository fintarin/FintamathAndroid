/**
 * Currently selected element.
 *
 * @type {HTMLSpanElement?}
 */
let selectedElem = null;

//---------------------------------------------------------------------------------------------------------//

mathTextView.oncut = onCut;
mathTextView.oncopy = onCopy;
mathTextView.onpaste = onPaste;

setInterval(function () {
  onSelectedElementChanged();
}, 10);

//---------------------------------------------------------------------------------------------------------//

/**
 * Converts selected HTML to math text and copies it to the clipboard.
 * Then deletes the selected text at the cursor.
 *
 * @param {Event} event - The cut event.
 */
function onCut(event) {
  onCopy(event);
  deleteAtCursor();
  event.preventDefault();
}

/**
 * Converts selected HTML to math text and copies it to the clipboard.
 *
 * @param {Event} event - The copy event.
 */
function onCopy(event) {
  const selection = window.getSelection();
  const range = selection.getRangeAt(0);
  const clonedSelection = range.cloneContents();

  const elem = createElement();
  elem.appendChild(clonedSelection);

  const text = toMathText(elem.innerHTML, mathTextView.isContentEditable);
  event.clipboardData.setData('text/plain', text);
  event.preventDefault();
}

/**
 * Converts math text from the clipboard to HTML and inserts it at the cursor.
 *
 * @param {Event} event - The paste event.
 * */
function onPaste(event) {
  const text = event.clipboardData.getData('text/plain');
  insertAtCursor(text);
  event.preventDefault();
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

    let prevSibling = elem.previousElementSibling;

    if (prevSibling !== null && getClassName(prevSibling) === unaryPostfixOperatorClass) {
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

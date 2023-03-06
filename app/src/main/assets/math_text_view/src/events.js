/**
 * Currently selected elem.
 *
 * @type {HTMLSpanElement?}
 */
let selectedElem = null;

//---------------------------------------------------------------------------------------------------------//

mathTextView.oncut = callOnCut;
mathTextView.oncopy = callOnCopy;
mathTextView.onpaste = callOnPaste;

setInterval(function () {
  callOnSelectedElementChanged();
}, 10);

//---------------------------------------------------------------------------------------------------------//

/**
 * Converts selected HTML to math text and copies it to the clipboard.
 * Then deletes the selected text at the cursor.
 *
 * @param {Event} event - The cut event.
 */
function callOnCut(event) {
  callOnCopy(event);
  deleteAtCursor();
  event.preventDefault();
}

/**
 * Converts selected HTML to math text and copies it to the clipboard.
 *
 * @param {Event} event - The copy event.
 */
function callOnCopy(event) {
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
function callOnPaste(event) {
  const text = event.clipboardData.getData('text/plain');
  insertAtCursor(text);
  event.preventDefault();
}

/**
 * Calls various functions to handle a change in mathTextView.
 */
function callOnChange() {
  reformatElement(mathTextView);
  insertEmptyTextsAndBorders(mathTextView);
  callOnSelectedElementChanged();

  if (undoStack.length > 0 && undoStack[undoStack.length - 1][0] === mathTextView.innerHTML) {
    undoStack.pop();
    return;
  }

  const mathText = toMathText(mathTextView.innerHTML, mathTextView.isContentEditable);
  Android.callOnTextChanged(mathText);
}

/**
 * Deselects the previously selected element and selects the element where the cursor is currently placed.
 */
function callOnSelectedElementChanged() {
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

    if (elem.className === undefined) {
      elem = elem.parentElement;
    }

    if (elem === selectedElem) {
      return;
    }

    if (elem.className === mathTextViewClass) {
      return;
    }

    selectedElem = elem;
    selectedElem.setAttribute('empty-hint', textEmptyHintSelected);
    selectedElem.style.textDecoration = 'underline dashed 0.05em';

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

    if (elem.className === mathTextViewClass) {
      return;
    }

    elem.setAttribute('empty-hint', '');
    elem.style.textDecoration = 'none';

    selectedElem = null;
  }
}

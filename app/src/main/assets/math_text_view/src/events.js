/**
 * Currently selected element.
 *
 * @type {HTMLSpanElement?}
 */
let selectedElem = null;

//---------------------------------------------------------------------------------------------------------//

window.oncontextmenu = onContextMenu;

mathTextView.onmousedown = onMouseDown;
mathTextView.onkeydown = onKeyDown;
mathTextView.onpaste = onPaste;

//---------------------------------------------------------------------------------------------------------//

setInterval(() => {
  onSelectedElementChanged();
}, 10);

//---------------------------------------------------------------------------------------------------------//

/**
 * Disable default cotext menu.
 *
 * @param {MouseEvent} event - The context menu event.
 */
function onContextMenu(event) {
  event.preventDefault();
}

/**
 * Handle the mousedown event.
 *
 * @param {MouseEvent} event - The mouse down event.
 */
function onMouseDown(event) {
  // Disable double click.
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
 * Handle the keydown event.
 *
 * @param {KeyboardEvent} event - The keydown event.
 */
function onKeyDown(event) {
  let shouldPreventDefault = true;

  if (event.ctrlKey) {
    switch (event.key) {
      case 'x': {
        navigator.clipboard.writeText(toMathText(mathTextView.innerHTML));
        clear();
        break;
      }
      case 'c': {
        navigator.clipboard.writeText(toMathText(mathTextView.innerHTML));
        break;
      }
      case 'z': {
        undo();
        break;
      }
      case 'Z':
      case 'y': {
        redo();
        break;
      }
      default: {
        shouldPreventDefault = false;
        break;
      }
    }
  } else {
    switch (event.key) {
      case 'Backspace': {
        deleteAtCursor();
        break;
      }
      case 'ArrowLeft':
      case 'ArrowUp': {
        moveCursorLeft();
        break;
      }
      case 'ArrowRight':
      case 'ArrowDown': {
        moveCursorRight();
        break;
      }
      default: {
        if (event.key.length == 1) {
          insertAtCursor(event.key);
        } else {
          shouldPreventDefault = false;
        }

        break;
      }
    }
  }

  if (shouldPreventDefault) {
    event.preventDefault();
  }
}

/**
 * Handle the paste event.
 *
 * @param {ClipboardEvent} event - The paste event.
 */
function onPaste(event) {
  const text = event.clipboardData.getData('text/plain').replace(/[\r\n]/g, '');
  insertAtCursor(text);
  event.preventDefault();
}

/**
 * Handle a text change in mathTextView.
 */
function onTextChange() {
  insertBordersRec(mathTextView);
  redrawSvgs(mathTextView);
  onSelectedElementChanged();

  let mathText = toMathText(mathTextView.innerHTML, mathTextView.isContentEditable);

  if (undoStack.length > 0 && undoStack[undoStack.length - 1][0] === mathText) {
    undoStack.pop();
    return;
  }

  let isMathTextComplete = isComplete(mathTextView) ? 'true' : 'false';

  // Math texts with a single and last character '=' are complete.
  if (mathText.length > 1 && mathText.endsWith(' =') && mathText.split('=').length - 1 === 1) {
    mathText = mathText.substring(0, mathText.length - 2);
    isMathTextComplete = 'true';
  }

  try {
    Android.onTextChange(mathText, isMathTextComplete);
  } catch (ReferenceError) {
    // do nothing
  }
}

/**
 * Deselect the previously selected element and select the element where the cursor is currently placed.
 */
function onSelectedElementChanged() {
  if (!mathTextView.isContentEditable) {
    return;
  }

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
   * Select the element where the cursor is currently placed and sets its style.
   *
   * @param {HTMLSpanElement} elem - The element where the cursor is currently placed.
   */
  function selectElement(elem) {
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
    selectedElem.setAttribute(hintAttr, textEmptyHintSelected);
    selectedElem.style.textDecoration = textDecorationAttr;

    if (selectedElem.innerHTML !== '') {
      selectedElem.style.textDecorationColor = getColorWithOpacity(mathTextView.style.color, textHintOpacity);
    }
  }

  /**
   * Deselect the element where the cursor is currently placed and sets its style.
   */
  function deselectElement(elem) {
    if (elem === null) {
      return;
    }

    if (getClassName(elem) === mathTextViewClass) {
      return;
    }

    elem.setAttribute(hintAttr, '');
    elem.style.textDecoration = textDecorationNoneAttr;

    selectedElem = null;
  }
}

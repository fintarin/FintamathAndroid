/**
 * Main math text view, which is displayed in the math_text_view component.
 *
 * @type {HTMLSpanElement}
 */
const mathTextView = document.getElementById(mathTextViewClass);

/**
 * Undo stack for undo operation.
 *
 * @type {HTMLSpanElement[]}
 */
let undoStack = [];

/**
 * Redo stack for redo operation.
 *
 * @type {HTMLSpanElement[]}
 */
let redoStack = [];

//---------------------------------------------------------------------------------------------------------//

/**
 * Convert math text to HTML and sets it as innerHTML of mathTextView.
 *
 * @param {string} mathText - The math text to convert to HTML.
 */
function setText(mathText) {
  addUndoState();

  mathTextView.innerHTML = toHtml(mathText, mathTextView.isContentEditable);

  if (mathTextView.isContentEditable) {
    setCursorToElementEnd(mathTextView);
  }

  onTextChange();
}

/**
 * Set the text color of mathTextView from the given.
 *
 * @param {string} color - The color to set.
 */
function setColor(color) {
  mathTextView.style.color = color;
  redrawSvgs(mathTextView);
}

/**
 * Set the contentEditable property of mathTextView from the given.
 *
 * @param {string} contentEditable - The value to set the contentEditable property to.
 */
function setContentEditable(contentEditable) {
  mathTextView.contentEditable = contentEditable;
  requestFocus();
}

/**
 * Convert MathText to HTML and inserts it at the current cursor position.
 *
 * @param {string} mathText - The math text to convert to HTML and insert.
 */
function insertAtCursor(mathText) {
  if (mathText.length === 0) {
    return;
  }

  addUndoState();

  const selection = window.getSelection();
  const range = selection.getRangeAt(0);

  let selNode = range.startContainer;
  let selParentElem = selNode.parentElement;

  const insElem = createElement();
  insElem.innerHTML = toHtml(mathText, mathTextView.isContentEditable);

  let insFirstChildElem = insElem.firstElementChild;
  let insLastChildElem = insElem.lastElementChild;

  if (selNode.nodeType === Node.TEXT_NODE) {
    const selOffset = range.startOffset;
    const oldTextElem = selParentElem;
    selParentElem = oldTextElem.parentElement;

    if (selOffset === 0) {
      insertChildren(selParentElem, insElem.children, oldTextElem);
    } else if (selOffset === oldTextElem.innerText.length) {
      insertChildren(selParentElem, insElem.children, oldTextElem.nextElementSibling);
    } else {
      selParentElem = oldTextElem.parentElement;

      const leftTextElem = createElement(textClass);
      leftTextElem.innerText = oldTextElem.innerText.substring(0, selOffset);
      selParentElem.insertBefore(leftTextElem, oldTextElem);

      const rightTextElem = createElement(textClass);
      rightTextElem.innerText = oldTextElem.innerText.substring(selOffset);
      selParentElem.insertBefore(rightTextElem, oldTextElem);

      selParentElem.removeChild(oldTextElem);
      insertChildren(selParentElem, insElem.children, rightTextElem);
    }
  } else if (getClassName(selNode) === mathTextViewClass) {
    insertChildren(selNode, insElem.children, null);
  } else {
    insertChildren(selParentElem, insElem.children, selNode.nextSibling);
  }

  ({ firstElem: insFirstChildElem, lastElem: insLastChildElem } = concatElementsOutside(
    insFirstChildElem,
    insLastChildElem
  ));

  setCursorBetweenElements(insFirstChildElem, insLastChildElem);

  {
    let insPrevFirstChildElem = insFirstChildElem.previousElementSibling;
    let insNextLastChildElem = insLastChildElem.nextElementSibling;

    if (insPrevFirstChildElem !== null) {
      concatTextElements(insPrevFirstChildElem, insPrevFirstChildElem.nextElementSibling);
    }
    if (insNextLastChildElem !== null) {
      concatTextElements(insNextLastChildElem.previousElementSibling, insNextLastChildElem);
    }
  }

  onTextChange();

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Set the cursor between the first and last element of the inserted children.
   *
   * @param {HTMLSpanElement} firstElem - The first element of inserted children.
   * @param {HTMLSpanElement} lastElem - The last element of inserted children.
   */
  function setCursorBetweenElements(firstElem, lastElem) {
    const parentElem = firstElem.parentElement;
    const firstIndex = Array.prototype.indexOf.call(parentElem.children, firstElem);
    const lastIndex = Array.prototype.indexOf.call(parentElem.children, lastElem);

    const firstTextHintElem = findFirstTextHintElement(parentElem, firstIndex, lastIndex);

    if (firstTextHintElem !== null) {
      setCursorToElementEnd(firstTextHintElem);
    } else if (
      lastElem.nextElementSibling !== null &&
      (!(lastElem instanceof HTMLSpanElement) ||
        operatorClasses.includes(getClassName(lastElem)) ||
        indexParentClasses.includes(getClassName(lastElem)))
    ) {
      setCursorToElementBegin(lastElem.nextElementSibling);
    } else {
      setCursorToElementEnd(lastElem);
    }
  }
}

/**
 * Delete a single character or an empty element at the current cursor position.
 * If the deletion is impossible, move the cursor to the left.
 */
function deleteAtCursor() {
  if (mathTextView.innerHTML === '') {
    return;
  }

  addUndoState();

  const selection = window.getSelection();
  const range = selection.getRangeAt(0);

  let elem = range.startContainer;
  if (elem.nodeType === Node.TEXT_NODE) {
    elem = elem.parentElement;
  }

  let prevElem = elem.previousSibling;

  if (getClassName(prevElem) === borderClass) {
    prevElem = prevElem.previousElementSibling;
  }

  if (!range.collapsed) {
    // TODO: implement deletion of selection
    range.collapse(true);
  } else if (range.startOffset !== 0) {
    if ([...elem.innerText].length === 1) {
      deleteChild(elem);
    } else {
      document.execCommand('delete', false, null);
    }
  } else if (prevElem !== null && areElementChildrenEmpty(prevElem)) {
    deleteChild(prevElem);
  } else {
    elem = elem.parentElement;
    while (childContainerClasses.includes(getClassName(elem))) {
      elem = elem.parentElement;
    }

    if (areElementChildrenEmpty(elem)) {
      deleteChild(elem);
    } else {
      moveCursorLeft();
    }
  }

  onTextChange();

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Delete the given element from its parent element.
   *
   * @param {HTMLSpanElement} elem - The element to delete.
   */
  function deleteChild(elem) {
    let parentElem = elem.parentElement;
    let prevElem = elem.previousElementSibling;
    let nextElem = elem.nextElementSibling;

    if (getClassName(prevElem) === functionNameClass) {
      let newPrevElem = prevElem.previousElementSibling;
      parentElem.removeChild(prevElem);
      prevElem = newPrevElem;
    }

    if (!deleteMatchingBracket(elem, parentElem, prevElem, nextElem)) {
      moveCursorLeft();
      return;
    }

    let isFirstElem = prevElem === null || (prevElem === parentElem.firstChild && isEmptyElement(prevElem));
    let isLastElem = nextElem === null || (nextElem === parentElem.lastChild && isEmptyElement(nextElem));

    parentElem.removeChild(elem);

    if (getClassName(parentElem) === mathTextViewClass && areElementChildrenEmpty(parentElem)) {
      parentElem.innerHTML = '';
      return;
    }

    if (prevElem === null) {
      prevElem = parentElem.firstElementChild;
    }

    if (getClassName(prevElem.nextElementSibling) === textHintClass) {
      prevElem = prevElem.nextElementSibling;
    }

    let newPrevElem = prevElem;
    ({ firstElem: newPrevElem, lastElem: nextElem } = concatElementsOutside(prevElem, prevElem));

    if (prevElem.parentElement !== parentElem || getClassName(elem) !== textClass) {
      prevElem = newPrevElem;
    }

    if (containerClasses.includes(getClassName(parentElem)) && parentElem.childElementCount === 0) {
      parentElem.appendChild(createElement(textHintClass));
      setCursorToTextElement(parentElem.firstElementChild);
    } else if (isFirstElem) {
      setCursorToElementBegin(parentElem);
    } else if (isLastElem) {
      setCursorToElementEnd(parentElem);
    } else if (textClasses.includes(getClassName(prevElem))) {
      setCursorToElementEnd(prevElem);
    } else if (containerClasses.includes(getClassName(prevElem))) {
      setCursorToElementBegin(prevElem.nextElementSibling);
    } else {
      setCursorToElementBegin(prevElem);
    }

    if (prevElem !== null) {
      concatTextElements(prevElem, prevElem.nextElementSibling);
    }

    /**
     * Delete the matching brackets element of the given element.
     * E.g. remove the close bracket in '( )'.
     *
     * @param {HTMLSpanElement} elem - The element that is deleted.
     * @param {HTMLSpanElement} parentElem - The parent element.
     * @param {HTMLSpanElement} prevElem - The previous element.
     * @param {HTMLSpanElement} nextElem - The next element.
     * @returns {boolean} Truth if we still have to delete the given element later, false otherwise.
     */
    function deleteMatchingBracket(elem, parentElem, prevElem, nextElem) {
      let result = true;

      if (getClassName(elem) in bracketMap) {
        result =
          nextElem !== null &&
          getClassName(nextElem) === textHintClass &&
          getClassName(nextElem.nextElementSibling) === bracketMap[getClassName(elem)];

        if (result) {
          parentElem.removeChild(nextElem.nextElementSibling);
        }

        result = result || getClassName(elem) === bracketPrefixClass;
      } else if (getClassName(elem) in bracketMapReversed && getClassName(elem) !== bracketPostfixClass) {
        result =
          prevElem !== null &&
          getClassName(prevElem) === textHintClass &&
          getClassName(prevElem.previousElementSibling) === bracketMapReversed[getClassName(elem)];

        if (result) {
          parentElem.removeChild(prevElem.previousElementSibling);
        }
      }

      return result;
    }
  }
}

/**
 * Clear the contents of mathTextView.
 */
function clear() {
  setText('');
}

/**
 * Undo the last change to mathTextView by restoring a previous state from the undo stack.
 * If the undo stack is empty, do nothing.
 */
function undo() {
  if (undoStack.length === 0) {
    return;
  }

  const selection = window.getSelection();
  const range = selection.getRangeAt(0);

  const oldElemPath = getElementPath(mathTextView, range.startContainer);
  const oldOffset = range.startOffset;

  redoStack.push([toMathText(mathTextView.innerHTML), oldElemPath, oldOffset]);

  const state = undoStack.pop();
  const mathText = state[0];
  const elemPath = state[1];
  const offset = state[2];

  mathTextView.innerHTML = toHtml(mathText, mathText.isContentEditable);
  onTextChange();
  restoreRange(mathTextView, elemPath, offset);
}

/**
 * Redo the last change to mathTextView by restoring a previous state from the redo stack.
 * If the redo stack is empty, do nothing.
 */
function redo() {
  if (redoStack.length === 0) {
    return;
  }

  const selection = window.getSelection();
  const range = selection.getRangeAt(0);

  const oldElemPath = getElementPath(mathTextView, range.startContainer);
  const oldOffset = range.startOffset;

  undoStack.push([toMathText(mathTextView.innerHTML), oldElemPath, oldOffset]);

  const state = redoStack.pop();
  const mathText = state[0];
  const elemPath = state[1];
  const offset = state[2];

  mathTextView.innerHTML = toHtml(mathText, mathTextView.isContentEditable);
  onTextChange();
  restoreRange(mathTextView, elemPath, offset);
}

/**
 * Add a new state to the undo stack and clear redo stack.
 */
function addUndoState() {
  if (!mathTextView.isContentEditable) {
    return;
  }

  let oldElemPath = [];
  let oldOffset = 0;

  const selection = window.getSelection();
  if (selection.rangeCount > 0) {
    const range = selection.getRangeAt(0);
    oldElemPath = getElementPath(mathTextView, range.startContainer);
    oldOffset = range.startOffset;
  }

  undoStack.push([toMathText(mathTextView.innerHTML), oldElemPath, oldOffset]);
  redoStack = [];
}

/**
 * Clear undo and redo stacks.
 */
function clearUndoStates() {
  undoStack = [];
  redoStack = [];
}

/**
 * Move the text cursor one character or one element to the left.
 */
function moveCursorLeft() {
  moveCursorLeftImpl();

  const selection = window.getSelection();
  const range = selection.getRangeAt(0);
  let elem = range.startContainer;

  if (elem.nodeType === Node.TEXT_NODE) {
    elem = elem.parentElement;
  }

  if (!textClasses.includes(getClassName(elem))) {
    moveCursorLeft();
  }

  //---------------------------------------------------------------------------------------------------------//

  /**
   * The cursor movement to the left itself.
   */
  function moveCursorLeftImpl() {
    const selection = window.getSelection();
    const range = selection.getRangeAt(0);

    let elem = range.startContainer;
    let offset = range.startOffset;

    if (elem.textContent !== null && offset > 0) {
      let delta = [...elem.textContent.substring(0, offset)].pop().length;
      setCursorToTextElement(elem, offset - delta);
      return;
    }

    if (getClassName(elem.previousElementSibling) === borderClass) {
      elem = elem.previousElementSibling;
    }

    while (
      elem.previousElementSibling === null ||
      emptyElementClasses.includes(getClassName(elem.previousElementSibling))
    ) {
      const parentElem = elem.parentElement;

      if (getClassName(parentElem) === mathTextViewClass) {
        return;
      }

      elem = parentElem;

      if (getClassName(elem.previousElementSibling) === borderClass) {
        elem = elem.previousElementSibling;
      }
    }

    setCursorToElementEnd(elem.previousElementSibling);
  }
}

/**
 * Move the text cursor one character or one element to the right.
 */
function moveCursorRight() {
  moveCursorRightImpl();

  const selection = window.getSelection();
  const range = selection.getRangeAt(0);
  let elem = range.startContainer;

  if (elem.nodeType === Node.TEXT_NODE) {
    elem = elem.parentElement;
  }

  if (!textClasses.includes(getClassName(elem))) {
    moveCursorRight();
  }

  //---------------------------------------------------------------------------------------------------------//

  /**
   * The cursor movement to the right itself.
   */
  function moveCursorRightImpl() {
    const selection = window.getSelection();
    const range = selection.getRangeAt(0);
    let elem = range.startContainer;
    let offset = range.startOffset;

    if (elem.textContent !== null && offset < elem.textContent.length) {
      let delta = [...elem.textContent.substring(offset)][0].length;
      setCursorToTextElement(elem, offset + delta);
      return;
    }

    if (getClassName(elem.nextElementSibling) === borderClass) {
      elem = elem.nextElementSibling;
    }

    while (elem.nextElementSibling === null || emptyElementClasses.includes(getClassName(elem.nextElementSibling))) {
      const parentElem = elem.parentElement;

      if (getClassName(parentElem) === mathTextViewClass) {
        return;
      }

      elem = parentElem;

      if (getClassName(elem.nextElementSibling) === borderClass) {
        elem = elem.nextElementSibling;
      }
    }

    setCursorToElementBegin(elem.nextElementSibling);
  }
}

/**
 * Set the focus to mathTextView.
 */
function requestFocus() {
  mathTextView.focus();
  setCursorToElementEnd(mathTextView);
}

/**
 * Remove the focus from mathTextView.
 */
function clearFocus() {
  mathTextView.blur();
}

function cut() {
  try {
    Android.onCut();
  } catch (ReferenceError) {
    navigator.clipboard.writeText(toMathText(mathTextView.innerHTML));
    clear();
  }
}

function copy() {
  try {
    Android.onCopy();
  } catch (ReferenceError) {
    navigator.clipboard.writeText(toMathText(mathTextView.innerHTML));
  }
}

function paste() {
  try {
    Android.onPaste();
  } catch (ReferenceError) {
    const text = event.clipboardData.getData('text/plain').replace(/[\r\n]/g, '');
    insertAtCursor(text);
  }
}

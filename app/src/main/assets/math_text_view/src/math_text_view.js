/**
 * Main math text view.
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
 * Converts MathText to HTML and sets it as innerHTML of mathTextView.
 *
 * @param {string} mathText - The math text to convert to HTML.
 */
function setText(mathText) {
  addUndoState();
  mathTextView.innerHTML = toHtml(mathText, mathTextView.isContentEditable);
  callOnChange();
}

/**
 * Sets the hint (:before element) of the mathTextView from the given.
 *
 * @param {string} hintText - The hint text to set.
 */
function setHint(hintText) {
  mathTextView.setAttribute('hint', hintText);
}

/**
 * Sets the text color of the mathTextView from the given.
 *
 * @param {string} color - The color to set.
 */
function setColor(color) {
  mathTextView.style.color = color;
}

/**
 * Sets the contentEditable property of the mathTextView from the given.
 *
 * @param {string} contentEditable - The value to set the contentEditable property to.
 */
function setContentEditable(contentEditable) {
  mathTextView.contentEditable = contentEditable;
  requestFocus();
}

/**
 * Converts MathText to HTML and inserts it at the current cursor position.
 *
 * @param {string} mathText - The math text to convert to HTML and insert.
 */
function insertAtCursor(mathText) {
  addUndoState();

  const selection = window.getSelection();
  const range = selection.getRangeAt(0);

  if (!range.collapsed) {
    deleteAtCursor();
  }

  let parentElem = range.startContainer;

  /** @type {HTMLSpanElement?} */
  let childPrevElem = null;

  /** @type {HTMLSpanElement?} */
  let childNextElem = null;

  while (
    parentElem.className !== mathTextViewClass &&
    parentElem.className !== textHintClass &&
    !supContainerClasses.includes(parentElem.className) &&
    !containerClasses.includes(parentElem.className) &&
    !subContainerClasses.includes(parentElem.className) &&
    !indexContainerClasses.includes(parentElem.className)
  ) {
    childPrevElem = parentElem;
    parentElem = parentElem.parentElement;
  }

  if (parentElem !== null && parentElem.innerHTML === '' && parentElem.className === textHintClass) {
    const prevSibling = parentElem.previousElementSibling;

    childPrevElem = parentElem;
    parentElem = parentElem.parentElement;
    parentElem.removeChild(childPrevElem);

    childPrevElem = prevSibling;
  }

  const startOffset = range.startOffset;

  if (
    childPrevElem !== null &&
    childPrevElem.innerText !== '' &&
    startOffset !== getUnicodeTextLength(childPrevElem.innerText)
  ) {
    if (startOffset === 0) {
      childNextElem = childPrevElem;
      childPrevElem = childPrevElem.previousElementSibling;
    } else if (childPrevElem.className === textClass || childPrevElem.className === textHintClass) {
      const oldElem = childPrevElem;

      childPrevElem = createElement(textClass);
      childPrevElem.innerText = oldElem.innerText.substring(0, startOffset);

      childNextElem = createElement(textClass);
      childNextElem.innerText = oldElem.innerText.substring(startOffset);

      parentElem.insertBefore(childPrevElem, oldElem);
      parentElem.insertBefore(childNextElem, oldElem);
      parentElem.removeChild(oldElem);
    } else {
      childNextElem = findNextElementByPreviousElement(parentElem, childPrevElem);
    }
  } else {
    childNextElem = findNextElementByPreviousElement(parentElem, childPrevElem);
  }

  if (childPrevElem !== null && childPrevElem.innerHTML === '' && childPrevElem.className === textClass) {
    const prevSibling = childPrevElem.previousElementSibling;
    parentElem.removeChild(childPrevElem);
    childPrevElem = prevSibling;
  }
  if (childNextElem !== null && childNextElem.innerHTML === '' && childNextElem.className === textClass) {
    const nextSibling = childNextElem.nextSibling;
    parentElem.removeChild(childNextElem);
    childNextElem = nextSibling;
  }

  const insElem = createElement();
  insElem.innerHTML = toHtml(mathText, mathTextView.isContentEditable);
  reformatElement(insElem);

  if (
    childPrevElem !== null &&
    childPrevElem.className !== textBorderClass &&
    childPrevElem.className !== binaryOperatorClass &&
    childPrevElem.className !== unaryPrefixOperatorClass &&
    insElem.firstChild !== null &&
    insElem.firstChild.innerHTML === '' &&
    (insElem.firstChild.className === textClass || insElem.firstChild.className === textHintClass)
  ) {
    insElem.removeChild(insElem.firstChild);
  }

  if (
    childNextElem !== null &&
    childNextElem.className !== textBorderClass &&
    childNextElem.className !== binaryOperatorClass &&
    childNextElem.className !== unaryPostfixOperatorClass &&
    insElem.lastChild !== null &&
    insElem.lastChild.innerHTML === '' &&
    (insElem.lastChild.className === textClass || insElem.lastChild.className === textHintClass)
  ) {
    insElem.removeChild(insElem.lastChild);
  }

  if (
    (operatorClasses.includes(insElem.lastChild.className) ||
      indexContainerClasses.includes(insElem.lastChild.className)) &&
    (childNextElem === null ||
      childNextElem.className === textBorderClass ||
      supContainerClasses.includes(childNextElem.className) ||
      containerClasses.includes(childNextElem.className))
  ) {
    insElem.appendChild(createElement(textClass));
  }

  while (insElem.firstChild) {
    parentElem.insertBefore(insElem.firstChild, childNextElem);
  }

  /** @type {number} */
  const childPrevElemIndex =
    childPrevElem !== null ? Array.prototype.indexOf.call(parentElem.children, childPrevElem) : -1;

  /** @type {number} */
  const childNextElemIndex =
    childNextElem !== null
      ? Array.prototype.indexOf.call(parentElem.children, childNextElem)
      : parentElem.children.length;

  const firstTextHintElem = getFirstTextHintElement(parentElem, childPrevElemIndex + 1, childNextElemIndex - 1);

  if (firstTextHintElem !== null) {
    setCursorToElementEnd(firstTextHintElem);
  } else if (childNextElem !== null && parentElem.children.length > 0) {
    setCursorToElementEnd(childNextElem.previousElementSibling);
  } else {
    setCursorToElementEnd(parentElem);
  }

  callOnChange();

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Returns the next sibling element of the given element, or the first child element of its parent element, or null if not found.
   *
   * @param {HTMLSpanElement} parentElem - The parent element to search for the next sibling element.
   * @param {HTMLSpanElement?} childPrevElem - The previous sibling element to start the search from.
   * @returns {HTMLSpanElement?} The next sibling element, or the first child element of the parent element, or null if not found.
   */
  function findNextElementByPreviousElement(parentElem, childPrevElem) {
    let childNextElem =
      childPrevElem !== null && childPrevElem !== parentElem.lastChild ? childPrevElem.nextSibling : null;

    if (parentElem.childElementCount > 0 && childPrevElem === null && childNextElem === null) {
      childNextElem = parentElem.firstChild;
    }

    return childNextElem;
  }
}

/**
 * Deletes a single character or an empty element at the current cursor position.
 * If the deletion is impossible, moves the cursor to the left.
 */
function deleteAtCursor() {
  const selection = window.getSelection();
  const range = selection.getRangeAt(0);

  if (!range.collapsed) {
    addUndoState();
    range.deleteContents();
    range.collapse(true);
  } else if (range.startOffset !== 0) {
    addUndoState();

    let elem = range.startContainer;

    if (elem.className === undefined) {
      elem = elem.parentElement;
    }

    if ([...elem.innerText].length === 1) {
      deleteChild(elem.parentElement, elem);
    } else {
      document.execCommand('delete', false, null);
    }
  } else {
    let parentElement = range.startContainer;

    while (parentElement.className === undefined) {
      parentElement = parentElement.parentElement;
    }

    const prevSibling = parentElement.previousElementSibling;

    if (prevSibling !== null && operatorClasses.includes(prevSibling.className)) {
      addUndoState();

      deleteChild(prevSibling.parentElement, prevSibling);

      const parentParentElem = parentElement.parentElement;

      if (parentParentElem !== null && parentElement.innerHTML === '') {
        deleteChild(parentParentElem, parentElement);
      }
    } else {
      if (parentElement.className === mathTextViewClass) {
        return;
      }

      let containerElem = parentElement;

      while (
        !supContainerClasses.includes(containerElem.className) &&
        !containerClasses.includes(containerElem.className) &&
        areElementChildrenEmpty(containerElem)
      ) {
        if (indexContainerClasses.includes(containerElem.className)) {
          break;
        }

        containerElem = parentElement;
        parentElement = parentElement.parentElement;
      }

      if (!areElementChildrenEmpty(containerElem)) {
        moveCursorLeft();
        return;
      }

      addUndoState();

      if (containerElem.className === bracketsClass && parentElement.className === functionClass) {
        containerElem = parentElement;
        parentElement = parentElement.parentElement;
      }

      deleteChild(parentElement, containerElem);
    }
  }

  callOnChange();

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Deletes a child element from a parent element.
   * Updates the cursor position.
   * Adds a text hint in the place of the child element. if necessary.
   *
   * @param {HTMLSpanElement} parentElem - The parent element from which to delete a child.
   * @param {HTMLSpanElement} childElem - The child element to delete.
   */
  function deleteChild(parentElem, childElem) {
    let prevSibling = childElem.previousElementSibling;

    parentElem.removeChild(childElem);

    if (areElementChildrenEmpty(parentElem)) {
      parentElem.innerHTML = '';

      if (
        supContainerClasses.includes(parentElem.className) ||
        containerClasses.includes(parentElem.className) ||
        subContainerClasses.includes(parentElem.className) ||
        indexContainerClasses.includes(parentElem.className)
      ) {
        let emptyTextHintElem = createElement(textHintClass);
        parentElem.appendChild(emptyTextHintElem);
        setCursorToElementEnd(emptyTextHintElem);
      } else {
        setCursorToElementEnd(parentElem);
      }
    } else if (prevSibling !== null) {
      if (textClasses.includes(prevSibling.className) || operatorClasses.includes(prevSibling.className)) {
        let prevElem = prevSibling;
        let nextElem = prevElem.nextElementSibling;

        if (
          nextElem !== null &&
          (operatorClasses.includes(nextElem.className) || indexContainerClasses.includes(nextElem.className))
        ) {
          if (indexContainerClasses.includes(nextElem.className)) {
            parentElem.insertBefore(createElement(textHintClass), prevElem.nextSibling);
            prevElem = prevElem.nextSibling;
          } else {
            switch (nextElem.className) {
              case unaryPostfixOperatorClass:
              case binaryOperatorClass: {
                if (unaryPrefixOperators.includes(nextElem.innerText)) {
                  nextElem.className = unaryPrefixOperatorClass;
                  parentElem.insertBefore(createElement(textClass), nextElem);
                } else {
                  parentElem.insertBefore(createElement(textHintClass), nextElem);
                }

                prevElem = prevElem.nextSibling;

                break;
              }
              case unaryPrefixOperatorClass: {
                parentElem.insertBefore(createElement(textClass), prevElem.nextSibling);
                prevElem = prevElem.nextSibling;
                break;
              }
            }
          }
        } else {
          switch (prevElem.className) {
            case unaryPrefixOperatorClass:
            case binaryOperatorClass: {
              parentElem.insertBefore(createElement(textHintClass), prevElem.nextSibling);
              prevElem = prevElem.nextSibling;
              break;
            }
            case unaryPostfixOperatorClass: {
              parentElem.insertBefore(createElement(textClass), prevElem.nextSibling);
              prevElem = prevElem.nextSibling;
              break;
            }
          }
        }

        setCursorToElementEnd(prevElem);
      } else {
        let elem = createElement(textClass);
        parentElem.insertBefore(elem, prevSibling.nextSibling);
        setCursorToElementEnd(elem);
      }
    } else {
      setCursorToElementBegin(parentElem.firstChild);
    }
  }
}

/**
 * Clears the contents of the math text view by setting its text to an empty string.
 */
function clear() {
  setText('');
}

/**
 * Undo the last change to the math text view by restoring a previous state from the undo stack.
 * If the undo stack is empty, does nothing.
 */
function undo() {
  if (undoStack.length === 0) {
    return;
  }

  const selection = window.getSelection();
  const oldRange = selection.getRangeAt(0);
  const oldElemPath = getElemPath(mathTextView, oldRange.startContainer);
  const oldOffset = oldRange.startOffset;

  redoStack.push([toMathText(mathTextView.innerHTML), oldElemPath, oldOffset]);

  const state = undoStack.pop();
  const mathText = state[0];
  const elemPath = state[1];
  const offset = state[2];

  mathTextView.innerHTML = toHtml(mathText);
  callOnChange();
  restoreRange(mathTextView, elemPath, offset);
}

/**
 * Redo the last change to the math text view by restoring a previous state from the redo stack.
 * If the redo stack is empty, does nothing.
 */
function redo() {
  if (redoStack.length === 0) {
    return;
  }

  const selection = window.getSelection();
  const oldRange = selection.getRangeAt(0);
  const oldElemPath = getElemPath(mathTextView, oldRange.startContainer);
  const oldOffset = oldRange.startOffset;

  undoStack.push([toMathText(mathTextView.innerHTML), oldElemPath, oldOffset]);

  const state = redoStack.pop();
  const mathText = state[0];
  const elemPath = state[1];
  const offset = state[2];

  mathTextView.innerHTML = toHtml(mathText);
  callOnChange();
  restoreRange(mathTextView, elemPath, offset);
}

/**
 * Adds a new state to the undo stack and clears redo stack.
 */
function addUndoState() {
  if (!mathTextView.isContentEditable) {
    return;
  }

  const selection = window.getSelection();
  const oldRange = selection.getRangeAt(0);
  const oldElemPath = getElemPath(mathTextView, oldRange.startContainer);
  const oldOffset = oldRange.startOffset;

  undoStack.push([toMathText(mathTextView.innerHTML), oldElemPath, oldOffset]);
  redoStack = [];
}

/**
 * Moves the text cursor one character or one element to the left.
 */
function moveCursorLeft() {
  moveCursorLeftImpl();

  const selection = window.getSelection();
  const range = selection.getRangeAt(0);
  const elem = range.startContainer;

  if (elem.className !== undefined && elem.className !== textClass && elem.className !== textHintClass) {
    moveCursorLeft();
  }

  //---------------------------------------------------------------------------------------------------------//

  /**
   * The cursor movement to the left itself
   */
  function moveCursorLeftImpl() {
    const selection = window.getSelection();
    const range = selection.getRangeAt(0);
    let elem = range.startContainer;
    let offset = range.startOffset;

    if (offset > 0) {
      setCursorToTextElement(elem, offset - 1);
      return;
    }

    while (elem.previousElementSibling === null) {
      const parentElem = elem.parentElement;

      if (parentElem.className === mathTextViewClass) {
        return;
      }

      elem = parentElem;
    }

    const prevSibling = elem.previousElementSibling;

    if (prevSibling.isContentEditable) {
      setCursorToElementEnd(prevSibling);
    } else {
      setCursorToElementEnd(prevSibling.previousElementSibling);
    }
  }
}

/**
 * Moves the text cursor one character or one element to the right.
 */
function moveCursorRight() {
  moveCursorRightImpl();

  const selection = window.getSelection();
  const range = selection.getRangeAt(0);
  const elem = range.startContainer;

  if (elem.className !== undefined && elem.className !== textClass && elem.className !== textHintClass) {
    moveCursorRight();
  }

  //---------------------------------------------------------------------------------------------------------//

  /**
   * The cursor movement to the right itself
   */
  function moveCursorRightImpl() {
    const selection = window.getSelection();
    const range = selection.getRangeAt(0);
    let elem = range.startContainer;
    let offset = range.startOffset;

    if (elem.textContent !== null && offset < elem.textContent.length) {
      setCursorToTextElement(elem, offset + 1);
      return;
    }

    while (elem.nextSibling === null) {
      const parentElem = elem.parentElement;

      if (parentElem.className === mathTextViewClass) {
        return;
      }

      elem = parentElem;
    }

    const nextSibling = elem.nextSibling;
    setCursorToElementBegin(nextSibling);
  }
}

/**
 * Sets the focus to mathTextView
 */
function requestFocus() {
  mathTextView.focus();
}

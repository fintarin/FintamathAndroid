/**
 * Converts from math text to HTML.
 *
 * @param {string} mathText - The math text to convert to HTML.
 * @param {boolean} [isEditable=false] - Whether the resulting HTML should be editable.
 * @returns {string} The HTML representation of the math text.
 */
function toHtml(mathText, isEditable = false) {
  for (let key in mathHtmlMap) {
    mathText = mathText.replace(makeRegexFromString(key), mathHtmlMap[key]);
  }

  if (isEditable) {
    for (let key in mathEditableHtmlMap) {
      mathText = mathText.replace(makeRegexFromString(key), mathEditableHtmlMap[key]);
    }
  }

  return toHtmlRec(mathText, 0, mathText.length - 1);

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Recursively converts from math text to HTML.
   *
   * @param {string} mathText - The math text to convert to HTML.
   * @param {number} start - The starting index of the portion of the math text to be converted.
   * @param {number} end - The ending index of the portion of the math text to be converted.
   * @returns {string} The HTML representation of the math text.
   */
  function toHtmlRec(mathText, start, end) {
    if (start > end) {
      return '';
    }

    let mainElem = createElement();
    let currentElem = createElement(textClass);
    mainElem.appendChild(currentElem);

    for (let i = start; i <= end; i++) {
      const ch = mathText[i];

      switch (ch) {
        case '(': {
          ({ start: i, currentElem: currentElem } = insertBrackets(mathText, i, end, mainElem, currentElem));
          continue;
        }
        case '^': {
          currentElem = insertIndex(mainElem, currentElem, supClass);
          continue;
        }
        case '_': {
          currentElem = insertIndex(mainElem, currentElem, subClass);
          continue;
        }
        case '/': {
          currentElem = insertFraction(mainElem, currentElem);
          continue;
        }
        case ' ': {
          currentElem = insertSpace(mainElem, currentElem);
          continue;
        }
      }

      if (binaryOperators.indexOf(ch) !== -1) {
        currentElem = removeLastEmptyTextElement(mainElem, currentElem);
        currentElem = insertOperator(mainElem, currentElem, binaryOperatorClass, ch);
        continue;
      }
      if (unaryPrefixOperators.indexOf(ch) !== -1) {
        currentElem = removeLastEmptyTextElement(mainElem, currentElem);
        currentElem = insertOperator(mainElem, currentElem, unaryPrefixOperatorClass, ch);
        continue;
      }
      if (unaryPostfixOperators.indexOf(ch) !== -1) {
        currentElem = removeLastEmptyTextElement(mainElem, currentElem);
        currentElem = insertOperator(mainElem, currentElem, unaryPostfixOperatorClass, ch);
        continue;
      }

      currentElem = insertLastEmptyTextElement(mainElem, currentElem);

      if (!isDigitOrPoint(ch)) {
        currentElem.innerHTML += ch;
      } else {
        if (currentElem.innerText !== '') {
          currentElem = createElement(textClass);
          mainElem.appendChild(currentElem);
        }

        i = appendString(mathText, i, end, currentElem);

        currentElem = createElement(textClass);
        mainElem.appendChild(currentElem);
      }
    }

    return mainElem.innerHTML;
  }

  /**
   * Appends a string of digits or a decimal point from mathText to currentElem.
   *
   * @param {string} mathText - The math text containing the string to append.
   * @param {number} start - The starting index in mathText to begin appending the string.
   * @param {number} end - The ending index in mathText to stop appending the string.
   * @param {HTMLSpanElement} currentElem - The element to append the string to.
   * @returns {number} The index of the last character appended to the string.
   */
  function appendString(mathText, start, end, currentElem) {
    let str = '';

    for (; start <= end && isDigitOrPoint(mathText[start]); start++) {
      str += mathText[start];
    }

    currentElem.innerHTML += str;

    return start - 1;
  }

  /**
   * Handles the insertion of whitespace character.
   *
   * @param {HTMLSpanElement} mainElem - The main element being constructed.
   * @param {HTMLSpanElement} currentElem - The current element being constructed.
   * @returns {HTMLSpanElement} The updated current element.
   */
  function insertSpace(mainElem, currentElem) {
    if (currentElem.innerHTML != '') {
      currentElem = createElement(textClass);
      mainElem.appendChild(currentElem);
    }

    return currentElem;
  }

  /**
   * Handles the insertion of plain operators.
   *
   * @param {HTMLSpanElement} mainElem - The main element being constructed.
   * @param {HTMLSpanElement} currentElem - The current element being constructed.
   * @param {string} operatorClass - The CSS class of the operator.
   * @param {string} operator - The operator itself.
   * @returns {HTMLSpanElement} The updated current element.
   */
  function insertOperator(mainElem, currentElem, operatorClass, operator) {
    let parentElem = mainElem;
    if (
      currentElem !== null &&
      currentElem.innerHTML === '' &&
      (subContainerClasses.includes(currentElem.className) || indexContainerClasses.includes(currentElem.className))
    ) {
      parentElem = currentElem;
    }

    currentElem = createElement(operatorClass);
    currentElem.innerHTML = operator;
    currentElem.contentEditable = 'false';
    parentElem.appendChild(currentElem);

    currentElem = createElement(textClass);
    parentElem.appendChild(currentElem);

    return currentElem;
  }

  /**
   * Handles the insertion of index operators.
   *
   * @param {HTMLSpanElement} mainElem - The main element being constructed.
   * @param {HTMLSpanElement} currentElem - The current element being constructed.
   * @param {string} indexClass - The CSS class of the index operator.
   * @returns {HTMLSpanElement} The updated current element.
   */
  function insertIndex(mainElem, currentElem, indexClass) {
    let parentElem = mainElem;
    if (
      currentElem !== null &&
      currentElem.innerHTML === '' &&
      (subContainerClasses.includes(currentElem.className) || indexContainerClasses.includes(currentElem.className))
    ) {
      parentElem = currentElem;
    }

    currentElem = createElement(indexClass);
    parentElem.appendChild(currentElem);

    return currentElem;
  }

  /**
   * Handles the insertion of fraction.
   *
   * @param {HTMLSpanElement} mainElem - The main element being constructed.
   * @param {HTMLSpanElement} currentElem - The current element being constructed.
   * @returns {HTMLSpanElement} The updated current element.
   */
  function insertFraction(mainElem, currentElem) {
    currentElem = removeLastEmptyTextElement(mainElem, currentElem);

    mainElem.removeChild(currentElem);
    currentElem.className = numeratorClass;

    const fracElem = createElement(fractionClass);
    mainElem.appendChild(fracElem);

    fracElem.appendChild(currentElem);

    currentElem = createElement(fractionLineClass);
    currentElem.contentEditable = 'false';
    fracElem.appendChild(currentElem);

    currentElem = createElement(denominatorClass);
    fracElem.appendChild(currentElem);

    return currentElem;
  }

  /**
   * Handles the insertion of brackets.
   *
   * @param {string} mathText - The math text to convert to HTML.
   * @param {number} start - The position of the open bracket.
   * @param {number} end - The last possible position of the closing bracket.
   * @param {HTMLSpanElement} mainElem - The main element being constructed.
   * @param {HTMLSpanElement} currentElem - The current element being constructed.
   * @returns {{start: any; currentElem: HTMLSpanElement;}} The updated start and the updated current element.
   */
  function insertBrackets(mathText, start, end, mainElem, currentElem) {
    const closingBracketPos = getClosingBracketPos(mathText, start, end);

    if (closingBracketPos !== -1) {
      const funcName = currentElem.innerHTML;

      currentElem = insertLastEmptyTextElement(mainElem, currentElem);

      if (funcName !== '') {
        switch (funcName) {
          case 'sqrt': {
            const sqrtElem = createElement(rootClass);
            sqrtElem.style.borderColor = getColorWithOpacity(mathTextView.style.color, linesOpacity);

            currentElem.innerHTML = '';
            currentElem.appendChild(sqrtElem);
            currentElem = sqrtElem;

            break;
          }
          case 'abs': {
            const absElem = createElement(absClass);
            absElem.style.borderColor = getColorWithOpacity(mathTextView.style.color, linesOpacity);

            currentElem.innerHTML = '';
            currentElem.appendChild(absElem);
            currentElem = absElem;

            break;
          }
          default: {
            const funcElem = createElement(functionClass);
            funcElem.setAttribute('function_name', funcName);

            let bracketsElem = createElement(bracketsClass);
            bracketsElem.style.borderColor = getColorWithOpacity(mathTextView.style.color, linesOpacity);
            funcElem.appendChild(bracketsElem);

            currentElem.innerHTML = '';
            currentElem.appendChild(funcElem);
            currentElem = funcElem.lastChild;

            break;
          }
        }

        currentElem.innerHTML += funcName;
      } else if (currentElem.className === textClass) {
        currentElem.className = bracketsClass;
        currentElem.style.borderColor = getColorWithOpacity(mathTextView.style.color, linesOpacity);
      }

      const hintElem = createElement(textHintClass);
      hintElem.innerHTML = toHtmlRec(mathText, start + 1, closingBracketPos - 1);
      currentElem.innerHTML = '';
      currentElem.appendChild(hintElem);

      currentElem = mainElem.lastChild;

      start = closingBracketPos;
    } else {
      currentElem.innerHTML += '(';

      currentElem = createElement(textClass);
      mainElem.appendChild(currentElem);
    }

    return { start, currentElem };
  }

  /**
   * Returns the position of the closing bracket for the given range of the mth text.
   *
   * @param {string} mathText - The math text to search in.
   * @param {number} start - The position of the open bracket.
   * @param {number} end - The last possible position of the closing bracket.
   * @returns {number} The position of the closing bracket or -1 if not found.
   * */
  function getClosingBracketPos(mathText, start, end) {
    let bracketsNum = 0;

    for (let i = start; i <= end; i++) {
      const ch = mathText[i];

      switch (ch) {
        case '(': {
          bracketsNum++;
          break;
        }
        case ')': {
          bracketsNum--;
          if (bracketsNum === 0) return i;
          break;
        }
      }
    }

    return -1;
  }

  /**
   * Removes the last empty text element from the main element, if it exists.
   *
   * @param {HTMLSpanElement} mainElem - The main element to remove the last empty text element from.
   * @param {HTMLSpanElement} currentElem - The current element that will be returned if the last empty text element is not removed.
   * @returns {HTMLSpanElement} The last child of the main element if the empty text element was removed, otherwise, the current element.
   */
  function removeLastEmptyTextElement(mainElem, currentElem) {
    if (mainElem.lastChild.className === textClass && mainElem.lastChild.innerHTML === '') {
      mainElem.removeChild(mainElem.lastChild);
      currentElem = mainElem.lastChild;
    }

    return currentElem;
  }

  /**
   * Inserts an empty text element at the end of the main element.
   *
   * @param {HTMLSpanElement} mainElem - The main element to insert the empty text element into.
   * @param {HTMLSpanElement} currentElem - The current element.
   * @returns {HTMLSpanElement} The last empty text element of the main element if it was created, otherwise, the current element.
   */
  function insertLastEmptyTextElement(mainElem, currentElem) {
    if (currentElem && currentElem.lastChild && currentElem.lastChild.className) {
      currentElem = createElement(text);
      mainElem.appendChild(currentElem);
    }

    return currentElem;
  }

  /**
   * Determines if a given character is a digit or a point.
   *
   * @param {string} ch - The character to check.
   * @returns {boolean} True if the character is a digit or a point, false otherwise.
   */
  function isDigitOrPoint(ch) {
    return ch === '.' || (ch >= '0' && ch <= '9');
  }
}

/**
 * Convert HTML to math text.
 *
 * @param {string} html - The input HTML to convert.
 * @param {boolean} [isEditable=false] - Whether or not the given HTML is editable.
 * @returns {string} The math text representation of HTML.
 */
function toMathText(html, isEditable = false) {
  const elem = createElement();
  elem.innerHTML = html;

  let text = toMathTextRec(elem);

  for (let key in mathHtmlMap) {
    text = text.replace(makeRegexFromString(mathHtmlMap[key]), key);
  }

  if (isEditable) {
    for (let key in mathEditableHtmlMap) {
      text = text.replace(makeRegexFromString(mathEditableHtmlMap[key]), key);
    }
  }

  return text;

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Convert element's HTML to math text recursively.
   *
   * @param {HTMLSpanElement} elem - The HTML element to convert.
   */
  function toMathTextRec(elem) {
    if (
      elem.childElementCount === 0 ||
      elem.className === textClass ||
      elem.className === textHintClass ||
      operatorClasses.includes(elem.className)
    ) {
      return elem.innerText;
    }

    switch (elem.className) {
      case functionClass: {
        return elem.getAttribute('function_name') + toMathTextChildren(elem);
      }
      case bracketsClass: {
        return '(' + toMathTextChildren(elem) + ')';
      }
      case absClass: {
        return 'abs(' + toMathTextChildren(elem) + ')';
      }
      case rootClass: {
        return 'sqrt(' + toMathTextChildren(elem) + ')';
      }
      case fractionClass: {
        return '(' + toMathTextChildren(elem.firstChild) + ')/(' + toMathTextChildren(elem.lastChild) + ')';
      }
      case supClass: {
        return '^(' + toMathTextChildren(elem) + ')';
      }
      case subClass: {
        return '_(' + toMathTextChildren(elem) + ')';
      }
      default: {
        return toMathTextChildren(elem);
      }
    }
  }

  /**
   * Convert element's children to math text.
   *
   * @param {HTMLSpanElement} elem - The HTML element whose children should be converted.
   */
  function toMathTextChildren(elem) {
    let text = '';

    for (let i = 0; i < elem.childElementCount; i++) {
      const childElem = elem.children[i];
      const childText = toMathTextRec(childElem);

      if (
        text.length > 0 &&
        text.charAt(text.length - 1) === ' ' &&
        (childElem.className === unaryPostfixOperatorClass || indexContainerClasses.includes(childElem.className))
      ) {
        text = text.substring(0, text.length - 1);
      }

      text += childText;

      if (childText !== '' && childElem.className !== unaryPrefixOperatorClass) {
        text += ' ';
      }
    }

    if (text.length > 0 && text.charAt(text.length - 1) === ' ') {
      return text.substring(0, text.length - 1);
    }

    return text;
  }
}

/**
 * Reformats an HTML element.
 *
 * @param {HTMLSpanElement} elem - The element to reformat.
 */
function reformatElement(elem) {
  const selection = window.getSelection();
  const range = selection.rangeCount > 0 ? selection.getRangeAt(0) : null;
  let selectedElem = range !== null ? range.startContainer : null;
  let selectedOffset = range !== null ? range.startOffset : -1;

  removeEmptyTextsAndBorders(elem);
  reformatTexts(elem);
  concatTexts(elem);
  reformatOperators(elem);
  reformatContainers(elem);

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Removes empty texts and borders recursively.
   *
   * @param {HTMLSpanElement} elem - The element to remove empty texts and borders from.
   */
  function removeEmptyTextsAndBorders(elem) {
    for (let i = 0; i < elem.childElementCount; i++) {
      const childElem = elem.children[i];
      removeEmptyTextsAndBorders(childElem);

      if (
        (childElem.className === textClass || childElem.className === textBorderClass) &&
        childElem.innerHTML === '' &&
        childElem !== selectedElem
      ) {
        elem.removeChild(childElem);
        i--;
      }
    }
  }

  /**
   * Reformats texts recursively.
   *
   * @param {HTMLSpanElement} elem - The element to reformat texts of.
   */
  function reformatTexts(elem) {
    for (let i = 0; i < elem.childElementCount; i++) {
      const childElem = elem.children[i];
      reformatTexts(childElem);

      if (
        childElem.childElementCount > 0 &&
        (childElem.className === textClass || childElem.className === textHintClass)
      ) {
        while (childElem.childElementCount !== 0) {
          elem.insertBefore(childElem.firstChild, childElem);
        }

        elem.removeChild(childElem);

        i--;
      }
    }
  }

  /**
   * Concatenates adjacent elements containing numbers recursively.
   *
   * @param {HTMLSpanElement} elem - The element for number concatenation.
   */
  function concatTexts(elem) {
    for (let i = 0; i < elem.childElementCount; i++) {
      const childElem = elem.children[i];
      concatTexts(childElem);

      const prevSibling = childElem.previousElementSibling;

      if (prevSibling !== null && isNotEmptyTextElement(childElem) && isNotEmptyTextElement(prevSibling)) {
        let offset = -1;

        if (childElem == selectedElem || childElem == selectedElem.parentElement) {
          offset = prevSibling.innerText.length + selectedOffset;
        } else if (prevSibling === selectedElem || prevSibling === selectedElem.parentElement) {
          offset = selectedOffset;
        }

        prevSibling.innerText += childElem.innerText;
        elem.removeChild(childElem);
        i--;

        selectedElem = prevSibling;

        if (offset !== -1) {
          selectedOffset = offset;
          setCursorToTextElement(selectedElem, selectedOffset);
        }
      }
    }

    //---------------------------------------------------------------------------------------------------------//

    /**
     * Determines whether the given element is a number element.
     *
     * @param {HTMLSpanElement} elem
     * @returns {boolean}
     */
    function isNotEmptyTextElement(elem) {
      return (elem.className === textClass || elem.className === textHintClass) && elem.innerText !== '';
    }
  }

  /**
   * Reformats binary and unary operators recursively.
   *
   * @param {HTMLSpanElement} elem - The element to format
   */
  function reformatOperators(elem) {
    for (let i = 0; i < elem.childElementCount; i++) {
      const childElem = elem.children[i];
      reformatOperators(childElem);
    }

    for (let i = 0; i < elem.childElementCount; i++) {
      const childElem = elem.children[i];

      if (
        childElem.className === binaryOperatorClass &&
        unaryPrefixOperators.includes(childElem.innerText) &&
        (childElem.previousElementSibling === null ||
          childElem.previousElementSibling.className === binaryOperatorClass ||
          childElem.previousElementSibling.className === unaryPrefixOperatorClass)
      ) {
        childElem.className = unaryPrefixOperatorClass;
      }

      if (
        childElem.className === unaryPrefixOperatorClass &&
        binaryOperators.includes(childElem.innerText) &&
        childElem.previousElementSibling !== null &&
        childElem.previousElementSibling.className !== binaryOperatorClass &&
        childElem.previousElementSibling.className !== unaryPrefixOperatorClass
      ) {
        childElem.className = binaryOperatorClass;
      }
    }

    for (let i = 0; i < elem.childElementCount; i++) {
      const childElem = elem.children[i];

      if (childElem.className === binaryOperatorClass) {
        if (
          childElem.previousElementSibling === null ||
          childElem.previousElementSibling.className === binaryOperatorClass
        ) {
          elem.insertBefore(createElement(textHintClass), childElem);
        }

        if (childElem.nextSibling === null || childElem.nextSibling.className === binaryOperatorClass) {
          elem.insertBefore(createElement(textHintClass), childElem.nextSibling);
          i++;
        }
      } else if (childElem.className === unaryPrefixOperatorClass) {
        if (childElem.nextSibling === null || childElem.nextSibling.className === binaryOperatorClass) {
          elem.insertBefore(createElement(textHintClass), childElem.nextSibling);
          i++;
        }
      } else if (
        childElem.className === unaryPostfixOperatorClass ||
        indexContainerClasses.includes(childElem.className)
      ) {
        if (
          childElem.previousElementSibling === null ||
          childElem.previousElementSibling.className === binaryOperatorClass
        ) {
          elem.insertBefore(createElement(textHintClass), childElem);
        }
      }
    }
  }

  /**
   * Reformats subcontainers and index containers recursively.
   *
   * @param {HTMLSpanElement} elem - The element to format.
   */
  function reformatContainers(elem) {
    for (let i = 0; i < elem.childElementCount; i++) {
      const childElem = elem.children[i];
      reformatContainers(childElem);
    }

    if (subContainerClasses.includes(elem.className) || indexContainerClasses.includes(elem.className)) {
      if (elem.childElementCount === 0) {
        const newChild = createElement(textHintClass);
        newChild.innerText = elem.innerText;

        elem.innerText = '';
        elem.appendChild(newChild);
      }
    }
  }
}

/**
 * Inserts empty text nodes and borders into container elements if necessary.
 *
 * @param {HTMLSpanElement} elem - The element to insert empty text nodes and borders for.
 */
function insertEmptyTextsAndBorders(elem) {
  for (let i = 0; i < elem.childElementCount; i++) {
    const childElem = elem.children[i];

    if (!supContainerClasses.includes(elem.className)) {
      if (!textClasses.includes(childElem.className)) {
        if (
          childElem.previousElementSibling === null ||
          !textClasses.includes(childElem.previousElementSibling.className)
        ) {
          elem.insertBefore(createElement(textClass), childElem);
        }

        if (childElem.nextSibling === null || !textClasses.includes(childElem.nextSibling.className)) {
          elem.insertBefore(createElement(textClass), childElem.nextSibling);
        }
      }
    }

    insertEmptyTextsAndBorders(childElem);
  }

  if (elem.className !== mathTextViewClass && elem.childElementCount > 0) {
    if (elem.firstChild.className === textClass && elem.firstChild.innerText !== '') {
      const borderElem = createElement(textBorderClass);
      elem.insertBefore(borderElem, elem.firstChild);
    }

    if (elem.lastChild.className === textClass && elem.lastChild.innerText !== '') {
      const borderElem = createElement(textBorderClass);
      elem.appendChild(borderElem);
    }
  }
}

/**
 * Returns the first text hint element found within the given range of child elements of a specified parent element.
 *
 * @param {HTMLSpanElement} parentElement - The parent element to search within.
 * @param {number} startIndex - The index of the first child element to check.
 * @param {number} endIndex - The index of the last child element to check.
 * @returns {HTMLSpanElement?} The first text hint elem found, or null if none were found.
 */
function getFirstTextHintElement(parentElement, startIndex, endIndex) {
  if (startIndex < 0) {
    startIndex = 0;
  }
  if (endIndex >= parentElement.childElementCount) {
    endIndex = parentElement.childElementCount - 1;
  }

  for (let i = startIndex; i <= endIndex; i++) {
    const childElem = parentElement.children[i];

    if (childElem.className === textHintClass) {
      return childElem;
    }

    if (childElem.children.length > 0) {
      return getFirstTextHintElement(childElem, 0, childElem.children.length - 1);
    }
  }

  return null;
}

/**
 * Sets the cursor to the beginning of an element's content.
 *
 * @param {HTMLSpanElement} elem - The element to set the cursor to the beginning of.
 */
function setCursorToElement(elem, offset) {
  if (elem.innerText === '') {
    setCursorToEmptyElement(elem, offset);
  } else {
    setCursorToTextElement(elem, offset);
  }
}

/**
 * Sets the cursor to the beginning of an element's content.
 *
 * @param {HTMLSpanElement} elem - The element to set the cursor to the beginning of.
 */
function setCursorToElementBegin(elem) {
  if (operatorClasses.includes(elem.className)) {
    setCursorToElementBegin(elem.nextSibling);
    return;
  }

  while (elem.innerHTML !== elem.innerText) {
    elem = elem.firstChild;
  }

  setCursorToElement(elem, 0);
}

/**
 * Sets the cursor to the ending of an element's content.
 *
 * @param {HTMLSpanElement} elem - The element to set the cursor to the ending of.
 */
function setCursorToElementEnd(elem) {
  if (operatorClasses.includes(elem.className)) {
    setCursorToElementBegin(elem.nextSibling);
    return;
  }

  while (elem.innerHTML !== elem.innerText) {
    elem = elem.lastChild;
  }

  setCursorToElement(elem, getUnicodeTextLength(elem.innerText));
}

/**
 * Sets the cursor to an empty element.
 *
 * @param {HTMLSpanElement} elem - The empty element to set the cursor to.
 * @param {number} offset - The offset to set the cursor to.
 */
function setCursorToEmptyElement(elem) {
  let tmpElem = null;
  tmpElem = createElement();
  tmpElem.innerHTML = '|';

  elem.appendChild(tmpElem);
  setCursorToTextElement(elem, 0);
  elem.removeChild(tmpElem);
}

/**
 * Sets the cursor to a text element.
 *
 * @param {HTMLSpanElement} elem - The text element to set the cursor to.
 * @param {number} offset - The offset to set the cursor to.
 */
function setCursorToTextElement(elem, offset) {
  const selection = window.getSelection();
  const range = selection.getRangeAt(0);

  while (elem.nodeType !== Node.TEXT_NODE) {
    elem = elem.firstChild;
  }

  let parentElem = elem.parentElement;
  parentElem.focus();

  range.setStart(elem, offset);
  range.collapse(true);

  const cursorPosition = range.getBoundingClientRect().right;
  const scrollOffset = (parentElem.offsetWidth * offset) / parentElem.innerText.length;

  const padding = parseFloat(window.getComputedStyle(mathTextView, null).getPropertyValue('padding-left'));

  if (cursorPosition < padding) {
    window.scrollTo(parentElem.offsetLeft + scrollOffset - padding, 0);
  } else if (cursorPosition > window.innerWidth - padding) {
    window.scrollTo(parentElem.offsetLeft - window.innerWidth + scrollOffset + padding, 0);
  }
}

/**
 * Restore the previous selection range within a given HTML element.
 *
 * @param {HTMLSpanElement} elem - The HTML element that contains the element with the specified path.
 * @param {number[]} elemPath - The array of indices indicating the path to the child element where the selection range was made.
 * @param {number} offset - The offset value of the selection range within the final child element.
 */
function restoreRange(elem, elemPath, offset) {
  for (const i of elemPath) {
    elem = elem.children[i];
  }

  setCursorToElement(elem, offset);
}

/**
 * Returns the path of indices of the given element relative to the main element.
 *
 * @param {HTMLSpanElement} mainElem - The main element to which the path is relative.
 * @param {HTMLSpanElement} elem - The element for which the path is calculated.
 * @returns {number[]} - An array of indices indicating the path of the element relative to the main element.
 */
function getElemPath(mainElem, elem) {
  let path = [];

  while (elem !== mainElem) {
    let parentElem = elem.parentNode;
    let index = Array.prototype.indexOf.call(parentElem.children, elem);

    if (index !== -1) {
      path.unshift(index);
    }

    elem = parentElem;
  }

  return path;
}

/**
 * Checks if all child elements of the given element are empty or not.
 *
 * @param {HTMLSpanElement} elem - The element to check.
 * @returns {boolean} Whether all child elements are empty or not.
 */
function areElementChildrenEmpty(elem) {
  if (elem.innerHTML === '') {
    return true;
  }

  for (let i = 0; i < elem.childElementCount; i++) {
    if (toMathText(elem.children[i].outerHTML) !== '') {
      return false;
    }
  }

  return true;
}

/**
 * Checks if the element does not contain empty text hint elements.
 *
 * @param {HTMLSpanElement} elem - The element to check.
 * @returns {boolean} Whether the element does not contain empty text hint elements.
 */
function isComplete(elem) {
  for (let i = 0; i < elem.childElementCount; i++) {
    const childElem = elem.children[i];

    if (childElem.className === textHintClass && childElem.innerHTML === '') {
      return false;
    }

    if (!isComplete(childElem)) {
      return false;
    }
  }

  return true;
}

/**
 * Creates a new HTML span element with the specified class name.
 *
 * @param {string} className - The class name to use for the new element.
 * @returns {HTMLSpanElement} The newly created HTML element.
 */
function createElement(className) {
  const elem = document.createElement('span');
  elem.className = className;
  return elem;
}

function getColorWithOpacity(color, opacity) {
  return color.replace(')', ',' + opacity.toString() + ')').replace('rgb', 'rgba');
}

/**
 * Creates a new RegExp object from the specified string.
 *
 * @param {string} str - The string to convert to a RegExp object.
 * @returns {RegExp} The newly created RegExp object.
 */
function makeRegexFromString(str) {
  return new RegExp(str.replace(/([.?*+^$[\]\\(){}|-])/g, '\\$1'), 'g');
}

/**
 * Returns the Unicode length of the specified string.
 *
 * @param {string} text - The string to measure.
 * @returns {number} The Unicode length of the string.
 */
function getUnicodeTextLength(text) {
  return [...text].length;
}

/**
 * Convert math text to HTML.
 *
 * @param {string} mathText - The math text to convert to HTML.
 * @param {boolean} [isEditable=false] - Whether the resulting HTML should be editable.
 * @returns {string} The HTML representation of the math text.
 */
function toHtml(mathText, isEditable = false) {
  mathText = mathText.replace(/\s/g, ' ');

  for (let key in mathHtmlMap) {
    mathText = mathText.replace(makeRegexFromString(key), mathHtmlMap[key]);
  }

  if (isEditable) {
    for (let key in mathEditableHtmlMap) {
      mathText = mathText.replace(makeRegexFromString(key), mathEditableHtmlMap[key]);
    }
  }

  const elem = toHtmlRec(mathText, 0, mathText.length - 1);
  insertHintsIntoContainersRec(elem);

  return elem.innerHTML;

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Recursively convert math text to HTML.
   *
   * @param {string} mathText - The math text to convert to HTML.
   * @param {number} start - The starting index of the portion of the math text to be converted.
   * @param {number} end - The ending index of the portion of the math text to be converted.
   * @returns {HTMLSpanElement} The element's HTML representation of the math text.
   */
  function toHtmlRec(mathText, start, end) {
    if (start > end) {
      return createElement(textHintClass);
    }

    /** @type {HTMLSpanElement} */
    const rootElem = createElement();

    /** @type {HTMLSpanElement?} */
    let childElem = null;

    for (let i = start; i <= end; i++) {
      const ch = mathText[i];

      switch (ch) {
        case openBracket:
        case closeBracket: {
          ({ start: i, childElem: childElem } = insertBrackets(rootElem, childElem, mathText, i, end));
          continue;
        }
        case divOperator: {
          childElem = insertFraction(chooseElement(rootElem, childElem));
          continue;
        }
        case supOperator: {
          childElem = insertIndex(chooseElement(rootElem, childElem), supClass);
          continue;
        }
        case subOperator: {
          childElem = insertIndex(chooseElement(rootElem, childElem), subClass);
          continue;
        }
        case space: {
          continue;
        }
      }

      if (unaryPrefixOperators.includes(ch) && indexContainerClasses.includes(getClassName(childElem))) {
        childElem = insertOperator(childElem, unaryPrefixOperatorClass, ch);
        continue;
      }

      {
        const operElemClassName = parseOperator(childElem, ch);

        if (operElemClassName !== undefinedClass) {
          childElem = insertOperator(rootElem, operElemClassName, ch);
          continue;
        }
      }

      let symbols = getLetters(mathText, i, end);

      if (symbols !== '') {
        let pos = i + symbols.length;

        if (pos <= end && mathText[pos] === openBracket) {
          ({ start: i, childElem: childElem } = insertBrackets(rootElem, childElem, mathText, pos, end, symbols));
          continue;
        }
      } else {
        symbols = ch;
      }

      if (specialSvgSymbols.includes(symbols)) {
        childElem = insertSpecialSymbol(chooseElement(rootElem, childElem), symbols);
        i += symbols.length - 1;
        continue;
      }

      if (i > start && mathText[i - 1] === space && getClassName(childElem) === textClass) {
        childElem = insertSpace(rootElem);
      } else if (childElem === null) {
        childElem = rootElem.appendChild(createElement(textClass));
      } else if (getClassName(childElem) !== textClass) {
        let parentElem = childElem;

        const newChildElem = createElement(textClass);

        if (
          !indexContainerClasses.includes(getClassName(childElem)) &&
          !childContainerClasses.includes(getClassName(childElem))
        ) {
          parentElem = parentElem.parentElement;
        }

        childElem = parentElem.appendChild(newChildElem);
      }

      if (childElem.innerHTML === '' && getClassName(childElem.previousElementSibling) === textClass) {
        let parentElem = childElem.parentElement;
        parentElem.removeChild(childElem);
        childElem = parentElem.lastElementChild;
      }

      childElem.innerHTML += symbols;
      i += symbols.length - 1;
    }

    insertHints(rootElem);
    insertEmptyTexts(rootElem);

    return rootElem;
  }

  /**
   * Handle the insertion of special symbols like Inf, ComplexInf.
   *
   * @param {HTMLSpanElement} elem - The element to insert special symbol.
   * @param {string} special - The symbol itself.
   * @returns {HTMLSpanElement} New child element.
   */
  function insertSpecialSymbol(elem, special) {
    switch (special) {
      case mathHtmlMap['Inf']: {
        elem = elem.appendChild(createSvg(infClass));
        break;
      }
      case mathHtmlMap['ComplexInf']: {
        elem = elem.appendChild(createSvg(complexInfClass));
        break;
      }
      case modOperator: {
        const operElemClass = parseOperator(null, special);
        elem = insertOperator(elem, operElemClass, special);
        break;
      }
    }

    return elem;
  }

  /**
   * Handle the insertion of simple operators like '+', '-'.
   *
   * @param {HTMLSpanElement} elem - The element to insert operator.
   * @param {string} operatorClass - The CSS class of the operator.
   * @param {string} operatorName - The name of the operator.
   * @returns {HTMLSpanElement} New child element.
   */
  function insertOperator(elem, operatorClass, operatorName) {
    const operElem = createElement(operatorClass);
    operElem.setAttribute(beforeContentAttr, operatorName);
    return elem.appendChild(operElem);
  }

  /**
   * Handle the insertion of index operator like '^', '_'.
   *
   * @param {HTMLSpanElement?} elem - The element to insert index.
   * @param {string} indexClass - The CSS class of the index operator.
   * @returns {HTMLSpanElement} New child element.
   */
  function insertIndex(elem, indexClass) {
    const indexElem = createElement(indexClass);

    if (indexContainerClasses.includes(getClassName(elem))) {
      elem = elem.parentElement;
    }

    return elem.appendChild(indexElem);
  }

  /**
   * Handle the insertion of fraction.
   *
   * @param {HTMLSpanElement?} elem - The element to insert fraction.
   * @returns {HTMLSpanElement} New child element.
   */
  function insertFraction(elem) {
    /** @type {HTMLSpanElement?} */
    let childElem = null;

    if (indexContainerClasses.includes(getClassName(elem))) {
      const parentElem = elem.parentElement;

      const prevElem = elem.previousElementSibling;
      parentElem.removeChild(prevElem);

      const oldElem = elem;
      parentElem.removeChild(oldElem);

      elem = parentElem;

      childElem = createElement();
      childElem.appendChild(prevElem);
      childElem.appendChild(oldElem);
    } else {
      childElem = createElement();

      let lastElem = elem.lastElementChild;

      if (lastElem !== null) {
        if (getClassName(lastElem) !== undefinedClass) {
          childElem.appendChild(lastElem);
        } else {
          insertChildren(childElem, lastElem.children, null);
          elem.removeChild(lastElem);
        }
      }
    }

    setClassName(childElem, numeratorClass);

    const fracElem = createElement(fractionClass);
    elem.appendChild(fracElem);

    fracElem.appendChild(childElem);

    childElem = createElement(denominatorClass);
    fracElem.appendChild(childElem);

    return childElem;
  }

  /**
   * Handle the insertion of brackets with or without function.
   *
   * @param {HTMLSpanElement} rootElem - The root element to insert brackets.
   * @param {HTMLSpanElement?} childElem - The child element to insert open bracket.
   * @param {string} mathText - The math text to convert to HTML.
   * @param {number} start - The position of the open bracket.
   * @param {number} end - The position of the close bracket.
   * @returns {{start: number; childElem: HTMLSpanElement;}} New start and the updated child element.
   */
  function insertBrackets(rootElem, childElem, mathText, start, end, funcName = '') {
    if (mathText[start] === closeBracket) {
      childElem = rootElem.appendChild(createSvg(closeBracketClass));
      return { start, childElem };
    }

    const closeBracketPos = getCloseBracketPos(mathText, start, end);

    if (closeBracketPos != -1) {
      let bracketsChildElem = toHtmlRec(mathText, start + 1, closeBracketPos - 1);

      if (funcName !== '') {
        bracketsChildElem = createFunctionElement(funcName, bracketsChildElem);
      }

      if (closeBracketPos < end && mathText[closeBracketPos + 1] === divOperator) {
        childElem = rootElem.appendChild(bracketsChildElem);
      } else if (
        indexContainerClasses.includes(getClassName(childElem)) ||
        getClassName(childElem) === denominatorClass
      ) {
        childElem = insertChildren(childElem, bracketsChildElem.children, null);
      } else {
        if (funcName === '') {
          putInBrackets(bracketsChildElem);
        }

        childElem = insertChildren(rootElem, bracketsChildElem.children, null);
      }

      start = closeBracketPos;
    } else {
      if (funcName !== '') {
        rootElem.appendChild(createFunctionNameElement(funcName));
      }

      childElem = rootElem.appendChild(createSvg(openBracketClass));
    }

    return { start, childElem };

    //---------------------------------------------------------------------------------------------------------//

    /**
     * Put the given element in brackets.
     *
     * @param {HTMLSpanElement} elem - The element to put in brackets.
     */
    function putInBrackets(elem) {
      elem.insertBefore(createSvg(openBracketClass), elem.firstElementChild);
      elem.appendChild(createSvg(closeBracketClass));
    }

    /**
     * Create a function element by its name and nested element.
     *
     * @param {HTMLSpanElement} elem - The element to put in function.
     * @param {string} funcName - The function name.
     * @returns {HTMLSpanElement} New function element.
     */
    function createFunctionElement(funcName, elem) {
      let isSpecialFunc = true;

      switch (funcName) {
        case sqrtFunction: {
          let sqrtContentElem = elem;
          setClassName(sqrtContentElem, rootContentClass);
          sqrtContentElem.style.borderColor = getColorWithOpacity(mathTextView.style.color, linesOpacity);

          elem = createElement(sqrtClass);
          elem.appendChild(createSvg(rootPrefixClass));
          elem.appendChild(sqrtContentElem);

          break;
        }
        case rootFunction: {
          let tokenElems = tokenizeByComma(elem);

          let rootIndexElem = tokenElems.length > 1 ? tokenElems[1] : createElement();
          setClassName(rootIndexElem, rootIndexClass);
          rootIndexElem.style.borderColor = getColorWithOpacity(mathTextView.style.color, linesOpacity);

          let rootContentElem = tokenElems.length > 1 ? tokenElems[0] : createElement();
          setClassName(rootContentElem, rootContentClass);
          rootContentElem.style.borderColor = getColorWithOpacity(mathTextView.style.color, linesOpacity);

          elem = createElement(rootClass);
          elem.appendChild(rootIndexElem);
          elem.appendChild(createSvg(rootPrefixClass));
          elem.appendChild(rootContentElem);

          break;
        }
        case logFunction: {
          let tokenElems = tokenizeByComma(elem);

          let logIndexElem = tokenElems.length > 1 ? tokenElems[0] : createElement();
          setClassName(logIndexElem, logIndexClass);

          let logContentElem = tokenElems.length > 1 ? tokenElems[1] : createElement();
          setClassName(logContentElem, logContentClass);

          elem = createElement(logClass);
          elem.appendChild(logIndexElem, elem.firstElementChild);
          elem.appendChild(createSvg(openBracketClass));
          elem.insertBefore(createFunctionNameElement(funcName), elem.firstElementChild);
          elem.appendChild(createSvg(closeBracketClass));
          elem.insertBefore(logContentElem, elem.lastElementChild);

          break;
        }
        case fracFunction: {
          let tokenElems = tokenizeByComma(elem);

          let numeratorElem = tokenElems.length > 1 ? tokenElems[0] : createElement();
          setClassName(numeratorElem, numeratorClass);

          let denominatorElem = tokenElems.length > 1 ? tokenElems[1] : createElement();
          setClassName(denominatorElem, denominatorClass);

          elem = createElement(fractionClass);
          elem.appendChild(numeratorElem);
          elem.appendChild(denominatorElem);

          break;
        }
        case absFunction: {
          elem.insertBefore(createSvg(prefixAbsClass), elem.firstChild);
          elem.appendChild(createSvg(postfixAbsClass));
          isSpecialFunc = false;
          break;
        }
        default: {
          putInBrackets(elem);
          elem.insertBefore(createFunctionNameElement(funcName), elem.firstElementChild);
          isSpecialFunc = false;
          break;
        }
      }

      if (isSpecialFunc) {
        let childElem = elem;
        elem = createElement();
        elem.appendChild(childElem);
      }

      return elem;
    }

    /**
     * Create a function name element by its name.
     *
     * @param {string} funcName - The function name.
     * @returns {HTMLSpanElement} New function name element.
     */
    function createFunctionNameElement(funcName) {
      const funcNameElem = createElement(functionNameClass);
      funcNameElem.setAttribute(beforeContentAttr, funcName);
      return funcNameElem;
    }

    /**
     *
     * @param {HTMLSpanElement} elem - The element to tokenize.
     * @returns {HTMLSpanElement[]}
     */
    function tokenizeByComma(elem) {
      let lastTokenElem = createElement();
      const tokenElems = [lastTokenElem];

      let bracketsCount = 0;

      while (elem.childElementCount > 0) {
        const childElem = elem.firstElementChild;

        switch (getClassName(childElem)) {
          case openBracketClass: {
            bracketsCount++;
            break;
          }
          case closeBracketClass: {
            bracketsCount--;
            break;
          }
        }

        let isCommaFound = false;

        if (childElem.getAttribute(beforeContentAttr) === comma) {
          if (bracketsCount === 0) {
            lastTokenElem = createElement();
            tokenElems.push(lastTokenElem);
            elem.removeChild(childElem);
            isCommaFound = true;
          }
        }

        if (!isCommaFound) {
          lastTokenElem.appendChild(childElem);
        }
      }

      return tokenElems;
    }
  }

  /**
   * Handle the insertion of space.
   *
   * @param {HTMLSpanElement?} elem - The element to insert space.
   * @returns {HTMLSpanElement?} New child element.
   */
  function insertSpace(elem) {
    if (elem.lastElementChild !== null && elem.lastElementChild.innerHTML !== '') {
      const spaceElem = createElement(textClass);
      return elem.appendChild(spaceElem);
    }

    return elem.lastChild;
  }

  /**
   * Insert text hints into containers recursively.
   *
   * @param {HTMLSpanElement} elem - The element to insert text hints.
   */
  function insertHintsIntoContainersRec(elem) {
    for (let i = 0; i < elem.childElementCount; i++) {
      const childElem = elem.children[i];
      insertHintsIntoContainersRec(childElem);
    }

    if (containerClasses.includes(getClassName(elem)) && elem.innerHTML === '') {
      elem.appendChild(createElement(textHintClass));
    }
  }

  /**
   * Get first letters in the given math text.
   *
   * @param {string} mathText - The math text to convert to HTML.
   * @param {number} start - The first position to search.
   * @param {number} end - The last position to search.
   * @returns {string} The updated start and New child element.
   */
  function getLetters(mathText, start, end) {
    let letters = '';

    for (let i = start; i <= end; i++) {
      if (!isLetter(mathText[i])) {
        break;
      }

      letters += mathText[i];
    }

    return letters;

    //---------------------------------------------------------------------------------------------------------//

    /**
     * Determine if a given character is a letter.
     *
     * @param {string} ch - The character to check.
     * @returns {boolean} True if the character is a letter, false otherwise.
     */
    function isLetter(ch) {
      return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }
  }

  /**
   * Return the position of the close bracket for the given range of the math text.
   *
   * @param {string} mathText - The math text to search in.
   * @param {number} start - The position of the open bracket.
   * @param {number} end - The last possible position of the close bracket.
   * @returns {number} The position of the close bracket or -1 if not found.
   * */
  function getCloseBracketPos(mathText, start, end) {
    let bracketsNum = 0;

    for (let i = start; i <= end; i++) {
      const ch = mathText[i];

      switch (ch) {
        case openBracket: {
          bracketsNum++;
          break;
        }
        case closeBracket: {
          bracketsNum--;
          if (bracketsNum === 0) return i;
          break;
        }
      }
    }

    return -1;
  }

  /**
   * Choose the element to insert into it.
   *
   * @param {HTMLSpanElement} rootElem - The root element being constructed.
   * @param {HTMLSpanElement} childElem - The child element being constructed.
   * @returns {HTMLSpanElement} Chosen element.
   */
  function chooseElement(rootElem, childElem) {
    if (childElem === null) {
      return rootElem;
    }

    if (
      containerClasses.includes(getClassName(childElem)) &&
      !parentContainerClasses.includes(getClassName(childElem))
    ) {
      return childElem;
    }

    return childElem.parentElement;
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

  text = text.replace(/\s+/g, ' ');

  return text;

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Convert element's HTML to math text recursively.
   *
   * @param {HTMLSpanElement} elem - The HTML element to convert.
   * @returns {string} The math text representation of the element.
   */
  function toMathTextRec(elem) {
    switch (getClassName(elem)) {
      case openBracketClass: {
        return openBracket;
      }
      case closeBracketClass:
      case postfixAbsClass: {
        return closeBracket;
      }
      case prefixAbsClass: {
        return absFunction + openBracket;
      }
      case functionNameClass: {
        return elem.getAttribute(beforeContentAttr);
      }
      case sqrtClass: {
        return sqrtFunction + putInBrackets(toMathTextChildren(elem));
      }
      case rootClass: {
        return (
          rootFunction +
          putInBrackets(toMathTextChildren(elem.children[2]) + comma + space + toMathTextChildren(elem.children[0]))
        );
      }
      case logClass: {
        return (
          logFunction +
          putInBrackets(toMathTextChildren(elem.children[1]) + comma + space + toMathTextChildren(elem.children[3]))
        );
      }
      case fractionClass: {
        return (
          fracFunction +
          putInBrackets(toMathTextChildren(elem.children[0]) + comma + space + toMathTextChildren(elem.children[1]))
        );
      }
      case supClass: {
        return supOperator + tryPutInBrackets(toMathTextChildren(elem));
      }
      case subClass: {
        return subOperator + tryPutInBrackets(toMathTextChildren(elem));
      }
      case textClass: {
        return elem.innerText;
      }
      case infClass: {
        return mathHtmlMap.Inf;
      }
      case complexInfClass: {
        return mathHtmlMap.ComplexInf;
      }
      default: {
        if (operatorClasses.includes(getClassName(elem))) {
          return elem.getAttribute(beforeContentAttr);
        }

        if (elem instanceof HTMLElement) {
          return toMathTextChildren(elem);
        }

        return '';
      }
    }
  }

  /**
   * Convert element's children to math text.
   *
   * @param {HTMLSpanElement} elem - The HTML element whose children should be converted.
   * @returns {string} The math text representation of the element children.
   */
  function toMathTextChildren(elem) {
    let text = '';

    let prevChildElem = null;

    for (let i = 0; i < elem.childElementCount; i++) {
      const childElem = elem.children[i];

      if (isEmptyElement(childElem)) {
        continue;
      }

      if (
        !bracketPostfixClasses.includes(getClassName(childElem)) &&
        !childContainerClasses.includes(getClassName(childElem)) &&
        !indexContainerClasses.includes(getClassName(childElem)) &&
        getClassName(childElem) !== unaryPostfixOperatorClass &&
        getClassName(prevChildElem) !== undefinedClass &&
        getClassName(prevChildElem) !== unaryPrefixOperatorClass &&
        getClassName(prevChildElem) !== functionNameClass &&
        !bracketPrefixClasses.includes(getClassName(prevChildElem))
      ) {
        text += space;
      }

      text += toMathTextRec(childElem);

      prevChildElem = childElem;
    }

    return text;
  }

  /**
   * Put the text in brackets.
   *
   * @param {String} text - The text to put in brackets.
   * @returns {String} The result.
   */
  function putInBrackets(text) {
    return openBracket + text + closeBracket;
  }

  /**
   * Put the text in brackets if its length > 1, otherwise do nothing.
   *
   * @param {String} text - The text to put in brackets.
   * @returns {String} The result.
   */
  function tryPutInBrackets(text) {
    if (text.length != 1) {
      return putInBrackets(text);
    }

    return text;
  }
}

/**
 * Set size and color of all SVG sub children in the given element.
 *
 * @param {HTMLSpanElement} elem - The element to search.
 */
function redrawSvg(elem) {
  if (elem.childElementCount === 0) {
    return;
  }

  const bracketMinHeight =
    bracketFirstScale *
    (getClassName(elem.firstChild) !== borderClass
      ? elem.firstChild.clientHeight
      : elem.firstChild.nextSibling.clientHeight);

  const bracketMaxHeightStack = [bracketMinHeight];

  /** @type {SVGSVGElement[]} */
  const openBracketElemsStack = [];

  for (let i = 0; i < elem.childElementCount; i++) {
    const childElem = elem.children[i];

    if (childElem instanceof SVGSVGElement) {
      setSvgColor(childElem);

      if (bracketClasses.includes(getClassName(childElem))) {
        setSvgHeight(childElem, bracketMinHeight);
      }
    }

    redrawSvg(childElem);

    let childElemHeight = childElem.clientHeight;

    switch (getClassName(childElem)) {
      case openBracketClass:
      case prefixAbsClass: {
        openBracketElemsStack.push(childElem);
        bracketMaxHeightStack.push(bracketMinHeight);
        break;
      }
      case closeBracketClass:
      case postfixAbsClass: {
        let height = bracketMaxHeightStack[bracketMaxHeightStack.length - 1];
        setSvgHeight(childElem, height);

        if (openBracketElemsStack.length > 0) {
          popHeightStack(bracketMaxHeightStack);
          setSvgHeight(openBracketElemsStack.pop(), height);
        } else {
          updateHeightStack(bracketMaxHeightStack, height * bracketNextScale);
        }

        break;
      }
    }

    if (indexContainerClasses.includes(getClassName(childElem))) {
      let top = Math.min(
        childElem.getBoundingClientRect().top,
        childElem.previousElementSibling.getBoundingClientRect().top
      );

      let bottom = Math.max(
        childElem.getBoundingClientRect().bottom,
        childElem.previousElementSibling.getBoundingClientRect().bottom
      );

      childElemHeight = bottom - top;
    }

    updateHeightStack(bracketMaxHeightStack, childElemHeight);
  }

  while (openBracketElemsStack.length > 0) {
    setSvgHeight(openBracketElemsStack.pop(), popHeightStack(bracketMaxHeightStack));
  }

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Update the height stack with the given height.
   *
   * @param {number[]} maxHeightStack - The height stack to update.
   * @param {number} height - The given height.
   */
  function updateHeightStack(maxHeightStack, height) {
    if (maxHeightStack[maxHeightStack.length - 1] < height) {
      maxHeightStack[maxHeightStack.length - 1] = height;

      for (let i = maxHeightStack.length - 1; i > 0; i--) {
        if (maxHeightStack[i - 1] < maxHeightStack[i]) {
          maxHeightStack[i - 1] = maxHeightStack[i];
        }
      }
    }
  }

  /**
   * Pop the height stack and updates it.
   *
   * @param {number[]} maxHeightStack - The height stack to pop.
   * @returns {number} The popped height.
   */
  function popHeightStack(maxHeightStack) {
    let height = maxHeightStack.pop();
    let prevHeight = height * bracketNextScale;

    if (maxHeightStack[maxHeightStack.length - 1] < prevHeight) {
      updateHeightStack(maxHeightStack, prevHeight);
    }

    return height;
  }

  /**
   * Set the SVG element height.
   *
   * @param {SVGSVGElement} elem - The SVG element to set its height.
   * @param {number} height - The height to set.
   */
  function setSvgHeight(elem, height) {
    elem.setAttribute('preserveAspectRatio', 'none');
    elem.style.height = height + 'px';
  }

  /**
   * Set the SVG icon color.
   *
   * @param {SVGSVGElement} elem - The SVG element to set its color.
   */
  function setSvgColor(elem) {
    const color = getColorWithOpacity(mathTextView.style.color, linesOpacity);
    elem.setAttribute('fill', color);
  }
}

/**
 * Concatenate the elements outside the first.
 *
 * @param {HTMLSpanElement} firstElem - The first element to concatenate.
 * @param {HTMLSpanElement} lastElem - The last element to concatenate.
 * @returns {{firstElem: HTMLSpanElement; lastElem: HTMLSpanElement;}} First and last children of concatenated element.
 */
function concatElementsOutside(firstElem, lastElem) {
  let parentElem = firstElem.parentElement;

  if (isEmptyElement(firstElem.previousElementSibling)) {
    parentElem.removeChild(firstElem.previousElementSibling);
  }

  if (isEmptyElement(lastElem.nextElementSibling)) {
    parentElem.removeChild(lastElem.nextElementSibling);
  }

  if (firstElem === lastElem) {
    if (isEmptyElement(firstElem)) {
      let elem = firstElem;
      firstElem = elem.previousElementSibling;
      lastElem = elem.nextElementSibling;
      parentElem.removeChild(elem);
    }
  } else {
    if (isEmptyElement(firstElem)) {
      let newFirstElem = firstElem.nextElementSibling;
      parentElem.removeChild(firstElem);
      firstElem = newFirstElem;
    }

    if (isEmptyElement(lastElem)) {
      let newLastElem = lastElem.previousElementSibling;
      parentElem.removeChild(lastElem);
      lastElem = newLastElem;
    }
  }

  if (firstElem === null) {
    firstElem = parentElem.firstElementChild;
  }

  if (lastElem === null) {
    lastElem = parentElem.lastElementChild;
  }

  let prevFirstElem = firstElem !== null ? firstElem.previousElementSibling : null;
  let nextFirstElem = firstElem !== null ? firstElem.nextElementSibling : null;
  let prevLastElem = lastElem !== null ? lastElem.previousElementSibling : null;
  let nextLastElem = lastElem !== null ? lastElem.nextElementSibling : null;

  if (operatorClasses.includes(getClassName(firstElem))) {
    setClassName(firstElem, parseOperator(prevFirstElem, firstElem.getAttribute(beforeContentAttr)));
  }

  if (operatorClasses.includes(getClassName(nextFirstElem))) {
    setClassName(nextFirstElem, parseOperator(firstElem, nextFirstElem.getAttribute(beforeContentAttr)));
  }

  if (operatorClasses.includes(getClassName(lastElem))) {
    setClassName(lastElem, parseOperator(prevLastElem, lastElem.getAttribute(beforeContentAttr)));
  }

  if (operatorClasses.includes(getClassName(nextLastElem))) {
    setClassName(nextLastElem, parseOperator(lastElem, nextLastElem.getAttribute(beforeContentAttr)));
  }

  insertHints(parentElem, getPreviousIndex(firstElem), getNextIndex(lastElem));
  insertEmptyTexts(parentElem, getPreviousIndex(firstElem), getNextIndex(lastElem));

  if (firstElem !== null && firstElem.previousElementSibling !== prevFirstElem) {
    firstElem = firstElem.previousElementSibling;
  }

  if (lastElem !== null && lastElem.nextElementSibling !== nextLastElem) {
    lastElem = lastElem.nextElementSibling;
  }

  return { firstElem, lastElem };

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Return the index of the previous element or of the given element or of the first element.
   *
   * @param {HTMLSpanElement} elem - The element to find its index.
   * @returns {number} The element index.
   */
  function getPreviousIndex(elem) {
    if (elem === null || elem.parentElement === null) {
      return 0;
    }

    let firstIndex = Array.prototype.indexOf.call(elem.parentElement.children, elem);

    if (elem.previousElementSibling !== null) {
      firstIndex--;
    }

    if (firstIndex < 0) {
      firstIndex = 0;
    }

    return firstIndex;
  }

  /**
   * Return the index of the next element or of the given element or of the last element.
   *
   * @param {HTMLSpanElement} elem - The element to find its index.
   * @returns {number} The element index.
   */
  function getNextIndex(elem) {
    if (elem === null || elem.parentElement === null) {
      return 0;
    }

    let lastIndex = Array.prototype.indexOf.call(elem.parentElement.children, elem);

    if (elem.nextElementSibling !== null) {
      lastIndex++;
    }

    if (lastIndex >= elem.parentElement.childElementCount) {
      lastIndex = elem.parentElement.childElementCount - 1;
    }

    return lastIndex;
  }
}

/**
 * Concatenate two text elements and restore the cursor position.
 *
 * @param {HTMLSpanElement?} leftElem - The left element to check.
 * @param {HTMLSpanElement?} rightElem - The right element to check.
 */
function concatTextElements(leftElem, rightElem) {
  if (getClassName(leftElem) !== textClass || getClassName(rightElem) !== textClass) {
    return;
  }

  const selection = window.getSelection();
  const range = selection.getRangeAt(0);

  let selNode = range.startContainer;
  if (selNode !== null && selNode.nodeType === Node.TEXT_NODE) {
    selNode = selNode.parentElement;
  }

  let selOffset = range.startOffset;
  if (rightElem === selNode) {
    selOffset += leftElem.innerText.length;
  }

  leftElem.innerText += rightElem.innerText;

  let parentElem = leftElem.parentElement;
  parentElem.removeChild(rightElem);

  if (getClassName(selNode) === textClass) {
    setCursorToTextElement(leftElem, selOffset);
  }
}

/**
 * Insert text hints from the start child to the end child of the given element.
 *
 * @param {HTMLSpanElement} elem - The element to insert text hints.
 * @param {number} start - The start index to check.
 * @param {number} end - The end index to check.
 */
function insertHints(elem, start = 0, end = elem.childElementCount - 1) {
  if (start < 0 || start >= elem.childElementCount) {
    return;
  }

  for (let i = start; i <= end; i++) {
    const childElem = elem.children[i];

    const className = getClassName(childElem);
    const prevElemClassName = childElem !== null ? getClassName(childElem.previousElementSibling) : undefinedClass;
    const nextElemClassName = childElem !== null ? getClassName(childElem.nextElementSibling) : undefinedClass;

    if (
      (className === binaryOperatorClass ||
        className === unaryPostfixOperatorClass ||
        indexContainerClasses.includes(className)) &&
      (prevElemClassName === undefinedClass ||
        prevElemClassName === binaryOperatorClass ||
        prevElemClassName === unaryPrefixOperatorClass ||
        prevElemClassName === openBracketClass ||
        prevElemClassName === prefixAbsClass)
    ) {
      elem.insertBefore(createElement(textHintClass), childElem);
      end++;
    }

    if (
      (className === binaryOperatorClass ||
        className === unaryPrefixOperatorClass ||
        className === openBracketClass ||
        className === prefixAbsClass) &&
      (nextElemClassName === undefinedClass ||
        nextElemClassName === binaryOperatorClass ||
        nextElemClassName === unaryPostfixOperatorClass ||
        nextElemClassName === closeBracketClass ||
        nextElemClassName === postfixAbsClass)
    ) {
      elem.insertBefore(createElement(textHintClass), childElem !== null ? childElem.nextElementSibling : null);
      end++;
      i++;
    }
  }
}

/**
 * Insert empty text elements from the start child to the end child of the given element.
 *
 * @param {HTMLSpanElement} elem - The element to insert empty texts.
 * @param {number} start - The start index to check.
 * @param {number} end - The end index to check.
 */
function insertEmptyTexts(elem, start = 0, end = elem.childElementCount - 1) {
  if (start < 0 || start >= elem.childElementCount) {
    return;
  }

  for (let i = start; i <= end; i++) {
    const childElem = elem.children[i];

    const className = getClassName(childElem);
    const prevElemClassName = childElem !== null ? getClassName(childElem.previousElementSibling) : undefinedClass;
    const nextElemClassName = childElem !== null ? getClassName(childElem.nextElementSibling) : undefinedClass;

    if (!parentContainerClasses.includes(getClassName(elem))) {
      if (
        (containerClasses.includes(className) ||
          bracketClasses.includes(className) ||
          specialSvgClasses.includes(className) ||
          className === unaryPrefixOperatorClass ||
          className === functionNameClass) &&
        prevElemClassName !== functionNameClass &&
        !textClasses.includes(prevElemClassName) &&
        prevElemClassName !== borderClass
      ) {
        elem.insertBefore(createElement(textClass), childElem);
        end++;
      }

      if (
        (containerClasses.includes(className) ||
          bracketClasses.includes(className) ||
          specialSvgClasses.includes(className) ||
          className === unaryPostfixOperatorClass) &&
        !textClasses.includes(nextElemClassName) &&
        nextElemClassName !== borderClass
      ) {
        elem.insertBefore(createElement(textClass), childElem !== null ? childElem.nextElementSibling : null);
        end++;
        i++;
      }
    }
  }
}

/**
 * Insert borders inside containers recursively.
 *
 * @param {HTMLSpanElement} elem - The element to insert borders.
 */
function insertBordersRec(elem) {
  if (!elem.isContentEditable) {
    return;
  }

  for (let i = 0; i < elem.childElementCount; i++) {
    const childElem = elem.children[i];

    if (
      (specialSvgClasses.includes(getClassName(childElem)) ||
        getClassName(childElem) === openBracketClass ||
        getClassName(childElem) === prefixAbsClass ||
        operatorClasses.includes(getClassName(childElem))) &&
      isNotEmptyTextElement(childElem.nextElementSibling)
    ) {
      elem.insertBefore(createElement(borderClass), childElem.nextElementSibling);
    }

    if (
      (specialSvgClasses.includes(getClassName(childElem)) ||
        getClassName(childElem) === closeBracketClass ||
        getClassName(childElem) === postfixAbsClass ||
        operatorClasses.includes(getClassName(childElem))) &&
      isNotEmptyTextElement(childElem.previousElementSibling)
    ) {
      elem.insertBefore(createElement(borderClass), childElem);
      i++;
    }

    insertBordersRec(childElem);
  }

  if (containerClasses.includes(getClassName(elem))) {
    if (isNotEmptyTextElement(elem.firstElementChild)) {
      const borderElem = createElement(borderClass);
      elem.insertBefore(borderElem, elem.firstElementChild);
    }

    if (isNotEmptyTextElement(elem.lastElementChild)) {
      const borderElem = createElement(borderClass);
      elem.appendChild(borderElem);
    }
  }
}

/**
 * Parse the operator class name from the operator name.
 *
 * @param {HTMLSpanElement} prevElem - The previous element.
 * @param {string} operName - The name of the operator.
 */
function parseOperator(prevElem, operName) {
  if (isEmptyElement(prevElem)) {
    prevElem = prevElem.previousElementSibling;
  }

  if (binaryOperators.includes(operName) && unaryPrefixOperators.includes(operName)) {
    if (
      getClassName(prevElem) === undefinedClass ||
      getClassName(prevElem) === binaryOperatorClass ||
      getClassName(prevElem) === unaryPrefixOperatorClass ||
      getClassName(prevElem) === openBracketClass ||
      getClassName(prevElem) === prefixAbsClass
    ) {
      return unaryPrefixOperatorClass;
    } else {
      return binaryOperatorClass;
    }
  }

  if (binaryOperators.includes(operName)) {
    return binaryOperatorClass;
  }

  if (unaryPrefixOperators.includes(operName)) {
    return unaryPrefixOperatorClass;
  }

  if (unaryPostfixOperators.includes(operName)) {
    return unaryPostfixOperatorClass;
  }

  return undefinedClass;
}

/**
 * Return the first text hint element found within the given range of child elements of the specified parent element.
 * Skips text hint elements before binary operators or indexes.
 *
 * @param {HTMLSpanElement} rootElem - The root element to search within.
 * @param {number} startIndex - The index of the first child element to check.
 * @param {number} endIndex - The index of the last child element to check.
 * @returns {HTMLSpanElement?} The first text hint elem found, or null if none were found.
 */
function findFirstTextHintElement(rootElem, startIndex, endIndex) {
  if (startIndex < 0) {
    startIndex = 0;
  }
  if (endIndex >= rootElem.childElementCount) {
    endIndex = rootElem.childElementCount - 1;
  }

  for (let i = startIndex; i <= endIndex; i++) {
    const childElem = rootElem.children[i];

    if (getClassName(childElem) === textHintClass) {
      const nextElem = childElem.nextElementSibling;

      if (
        nextElem === null ||
        (getClassName(nextElem) !== binaryOperatorClass && !indexContainerClasses.includes(getClassName(nextElem)))
      ) {
        return childElem;
      }
    }

    if (childElem instanceof HTMLElement) {
      let resElem = findFirstTextHintElement(childElem, 0, childElem.childElementCount - 1);

      if (resElem !== null) {
        return resElem;
      }
    }
  }

  return null;
}

/**
 * Set the cursor to the text element.
 *
 * @param {HTMLSpanElement} elem - The text element to set the cursor.
 */
function setCursorToTextElement(elem, offset) {
  if (elem.innerText === '') {
    setCursorToEmptyElement(elem);
  } else {
    setCursorToNonEmptyElement(elem, offset);
  }

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Set the cursor to the empty element.
   *
   * @param {HTMLSpanElement} elem - The empty element to set the cursor to.
   */
  function setCursorToEmptyElement(elem) {
    const tmpElem = createElement();
    tmpElem.innerHTML = '.'; // Just random text.

    elem.appendChild(tmpElem);
    setCursorToTextElement(elem, 0);
    elem.removeChild(tmpElem);
  }

  /**
   * Set the cursor to the non empty element.
   *
   * @param {HTMLSpanElement} elem - The non empty element to set the cursor to.
   * @param {number} offset - The offset to set the cursor to.
   */
  function setCursorToNonEmptyElement(elem) {
    const selection = window.getSelection();

    if (selection.rangeCount === 0) {
      selection.addRange(new Range());
    }

    const range = selection.getRangeAt(0);

    while (elem.nodeType !== Node.TEXT_NODE) {
      elem = elem.firstChild;
    }

    const rootElem = elem.parentElement;
    rootElem.focus();

    range.setStart(elem, offset);
    range.collapse(true);

    const cursorPosition = range.getBoundingClientRect().right;
    const scrollOffset = (rootElem.offsetWidth * offset) / rootElem.innerText.length;
    const padding = parseFloat(window.getComputedStyle(mathTextView, null).getPropertyValue('padding-left'));

    if (cursorPosition < padding) {
      window.scrollTo(rootElem.offsetLeft + scrollOffset - padding, 0);
    } else if (cursorPosition > window.innerWidth - padding) {
      window.scrollTo(rootElem.offsetLeft - window.innerWidth + scrollOffset + padding, 0);
    }
  }
}

/**
 * Set the cursor to the beginning of the element's content.
 *
 * @param {(HTMLSpanElement | SVGElement)?} elem - The element to set the cursor to the beginning of.
 */
function setCursorToElementBegin(elem) {
  if (elem === null) {
    return;
  }

  if (
    !textClasses.includes(getClassName(elem)) &&
    !containerClasses.includes(getClassName(elem)) &&
    getClassName(elem) !== mathTextViewClass
  ) {
    if (elem.nextElementSibling !== null) {
      setCursorToElementBegin(elem.nextElementSibling);
    } else {
      let parentElem = elem.parentElement;

      if (parentElem.nextElementSibling === null) {
        parentElem = parentElem.parentElement;
      }

      setCursorToElementBegin(parentElem.nextElementSibling);
    }
    return;
  }

  while (elem instanceof HTMLElement && elem.innerHTML !== elem.innerText) {
    elem = elem.firstElementChild;
  }

  if (!textClasses.includes(getClassName(elem))) {
    setCursorToElementBegin(elem.nextElementSibling);
    return;
  }

  setCursorToTextElement(elem, 0);
}

/**
 * Set the cursor to the ending of the element's content.
 *
 * @param {(HTMLSpanElement | SVGElement)?} elem - The element to set the cursor to the ending of.
 */
function setCursorToElementEnd(elem) {
  if (elem === null) {
    return;
  }

  if (
    !textClasses.includes(getClassName(elem)) &&
    !containerClasses.includes(getClassName(elem)) &&
    getClassName(elem) !== mathTextViewClass
  ) {
    if (elem.previousElementSibling !== null) {
      setCursorToElementEnd(elem.previousElementSibling);
    } else {
      let parentElem = elem.parentElement;

      if (parentElem.previousElementSibling === null) {
        parentElem = parentElem.parentElement;
      }

      setCursorToElementEnd(parentElem.previousElementSibling);
    }
    return;
  }

  while (elem instanceof HTMLElement && elem.innerHTML !== elem.innerText) {
    elem = elem.lastElementChild;
  }

  if (!textClasses.includes(getClassName(elem))) {
    setCursorToElementEnd(elem.previousElementSibling);
    return;
  }

  setCursorToTextElement(elem, elem.innerText.length);
}

/**
 * Restore the previous selection range within the given HTML element.
 *
 * @param {HTMLSpanElement} elem - The HTML element that contains the element with the specified path.
 * @param {number[]} elemPath - The array of indices indicating the path to the child element where the selection range was made.
 * @param {number} offset - The offset value of the selection range within the final child element.
 */
function restoreRange(elem, elemPath, offset) {
  for (const i of elemPath) {
    elem = elem.children[i];
  }

  setCursorToTextElement(elem, offset);
}

/**
 * Return the path of indices of the given element relative to The root element.
 *
 * @param {HTMLSpanElement} rootElem - The root element to which the path is relative.
 * @param {HTMLSpanElement} elem - The element for which the path is calculated.
 * @returns {number[]} - An array of indices indicating the path of the element relative to The root element.
 */
function getElementPath(rootElem, elem) {
  let path = [];

  while (elem !== rootElem) {
    const rootElem = elem.parentElement;
    const index = Array.prototype.indexOf.call(rootElem.children, elem);

    if (index !== -1) {
      path.unshift(index);
    }

    elem = rootElem;
  }

  return path;
}

/**
 * Check if the math text of the element is valid.
 *
 * @param {HTMLSpanElement} elem - The element to check.
 * @returns {boolean} Whether the element is complete.
 */
function isComplete(elem) {
  let openBracketsCount = 0;

  for (let i = 0; i < elem.childElementCount; i++) {
    const childElem = elem.children[i];

    if (getClassName(childElem) === textHintClass || openBracketsCount < 0) {
      return false;
    }

    if (getClassName(childElem) === openBracketClass) {
      openBracketsCount++;
    } else if (getClassName(childElem) === closeBracketClass) {
      openBracketsCount--;
    }

    if (!isComplete(childElem)) {
      return false;
    }
  }

  return openBracketsCount === 0;
}

/**
 * Determine whether the given element is a non empty text element.
 *
 * @param {HTMLSpanElement} elem
 * @returns {boolean} Whether the given element is a non empty text element.
 */
function isNotEmptyTextElement(elem) {
  return getClassName(elem) === textClass && elem.innerText !== '';
}

/**
 * Determine whether the given element is a empty element.
 *
 * @param {HTMLSpanElement} elem
 * @returns {boolean} Whether the given element is a empty element.
 */
function isEmptyElement(elem) {
  return (textClasses.includes(getClassName(elem)) || getClassName(elem) === borderClass) && elem.innerHTML === '';
}

/**
 * Check if all child elements of the given element are empty or not.
 *
 * @param {HTMLSpanElement} elem - The element to check.
 * @returns {boolean} Whether all child elements are empty or not.
 */
function areElementChildrenEmpty(elem) {
  if (elem.innerHTML === '') {
    return true;
  }

  for (let i = 0; i < elem.childElementCount; i++) {
    const childElem = elem.children[i];

    if (
      parentContainerClasses.includes(getClassName(elem)) &&
      (childElem instanceof SVGElement || getClassName(childElem) === functionNameClass)
    ) {
      continue;
    }

    if (toMathText(childElem.outerHTML) !== '') {
      return false;
    }
  }

  return true;
}

/**
 * Insert children into the element.
 *
 * @param {HTMLSpanElement} elem - The element to insert children.
 * @param {HTMLCollection} children - The children to insert.
 * @param {HTMLSpanElement?} beforeElem - The element before which the children are inserted.
 * @returns {HTMLSpanElement} New child element.
 */
function insertChildren(elem, children, beforeElem) {
  while (children.length > 0) {
    elem.insertBefore(children[0], beforeElem);
  }

  return elem.lastChild;
}

/**
 * Return the class name of the HTML or SVG element.
 *
 * @param {Node?} elem - The HTML or SVG element.
 * @returns {string} The element class name.
 */
function getClassName(elem) {
  if (elem instanceof HTMLElement) {
    return elem.className;
  }

  if (elem instanceof SVGElement) {
    return elem.getAttribute('class');
  }

  return undefinedClass;
}

/**
 * Set the class name of the HTML or SVG element.
 *
 * @param {HTMLOrSVGElement?} elem - The HTML or SVG element.
 * @param {string} className The class name to set.
 */
function setClassName(elem, className) {
  if (elem === null) {
    return;
  }

  elem.setAttribute('class', className);
}

/**
 * Create a new HTML span element with the specified class name.
 *
 * @param {string} className - The class name to use for new element.
 * @param {boolean} [isEditable=false] - Whether new element should be editable.
 * @returns {HTMLSpanElement} Newly created HTML element.
 */
function createElement(className) {
  const elem = document.createElement('span');
  setClassName(elem, className);
  return elem;
}

/**
 * Create a cached SVG element.
 *
 * @param {string} className - The class name to use for new element.
 * @returns {SVGSVGElement} Created SVG element.
 */
function createSvg(className) {
  return svgElementsMap[[className]].cloneNode(true);
}

/**
 * Create an SVG element with the specified class name, path and view box.
 *
 * @param {string} className - The class name to use for new element.
 * @param {string} path - The SVG path to draw.
 * @param {string} viewBox - The SVG view box.
 * @returns {SVGSVGElement} Newly created SVG element.
 */
function createNewSvg(className, path, viewBox) {
  const pathElem = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  pathElem.setAttribute('d', path);

  const svgElem = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svgElem.setAttribute('class', className);
  svgElem.setAttribute('viewBox', viewBox);
  svgElem.appendChild(pathElem);

  return svgElem;
}

function getColorWithOpacity(color, opacity) {
  return color.replace(closeBracket, ',' + opacity.toString() + closeBracket).replace('rgb', 'rgba');
}

/**
 * Create a new RegExp object from the specified string.
 *
 * @param {string} str - The string to convert to a RegExp object.
 * @returns {RegExp} Newly created RegExp object.
 */
function makeRegexFromString(str) {
  return new RegExp(str.replace(/([.?*+^$[\]\\(){}|-])/g, '\\$1'), 'g');
}

/**
 * Return the Unicode length of the specified string.
 *
 * @param {string} text - The string to measure.
 * @returns {number} The Unicode length of the string.
 */
function getUnicodeTextLength(text) {
  return [...text].length;
}

/**
 * Remove the first and the last spaces from the given string.
 *
 * @param {String} str - The string to remove spaces from.
 * @returns {String} - The result.
 */
function cutSpaces(str) {
  if (str.length > 1 && str[0] === space) {
    str = str.substring(1);
  }

  if (str.length > 1 && str[str.length - 1] === space) {
    str = str.substring(0, str.length - 1);
  }

  return str;
}

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
  insertHintsRec(elem);
  insertEmptyTextsRec(elem);

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
      let symbols = getLetters(mathText, i, end);

      if (symbols !== '') {
        let pos = i + symbols.length;

        // Insert a function
        if (pos <= end && (mathText[pos] === openBracket || mathText[pos] === openSquareBracket)) {
          ({ start: i, childElem: childElem } = insertBrackets(rootElem, childElem, mathText, pos, end, symbols));
          continue;
        }
      } else {
        symbols = mathText[i];
      }

      const prevIndex = i;
      i += symbols.length - 1;

      // Insert special symbols
      switch (symbols) {
        case openBracket:
        case openSquareBracket: {
          ({ start: i, childElem: childElem } = insertBrackets(rootElem, childElem, mathText, prevIndex, end));
          continue;
        }
        case closeBracket:
        case closeSquareBracket: {
          childElem = rootElem.appendChild(createSvg(bracketPostfixClass));
          continue;
        }
        case divOperator: {
          childElem = insertFraction(chooseElement(rootElem, childElem));
          continue;
        }
        case supOperator: {
          childElem = insertIndex(chooseElement(rootElem, childElem), supParentClass, supClass);
          continue;
        }
        case subOperator: {
          childElem = insertIndex(chooseElement(rootElem, childElem), subParentClass, subClass);
          continue;
        }
        case piConst: {
          childElem = chooseElement(rootElem, childElem).appendChild(createSvg(piClass));
          continue;
        }
        case eConst: {
          childElem = chooseElement(rootElem, childElem).appendChild(createSvg(eClass));
          continue;
        }
        case iConst: {
          childElem = chooseElement(rootElem, childElem).appendChild(createSvg(iClass));
          continue;
        }
        case infConst: {
          childElem = chooseElement(rootElem, childElem).appendChild(createSvg(infClass));
          continue;
        }
        case complexInfConst: {
          childElem = chooseElement(rootElem, childElem).appendChild(createSvg(complexInfClass));
          continue;
        }
        case space: {
          childElem = rootElem.lastElementChild;
          continue;
        }
      }

      // Insert an unary prefix operator into the index container
      if (unaryPrefixOperators.includes(symbols) && indexChildClasses.includes(getClassName(childElem))) {
        childElem = insertOperator(childElem, unaryPrefixOperatorClass, symbols);
        continue;
      }

      // Insert an operator
      {
        const operElemClassName = parseOperator(childElem, symbols);

        if (operElemClassName !== undefinedClass) {
          childElem = insertOperator(rootElem, operElemClassName, symbols);
          continue;
        }
      }

      // Insert a text element
      if (childElem === null) {
        childElem = rootElem.appendChild(createElement(textClass));
      } else if (getClassName(childElem) !== textClass) {
        let parentElem = childElem;

        const newChildElem = createElement(textClass);

        if (!childContainerClasses.includes(getClassName(childElem))) {
          parentElem = parentElem.parentElement;
        }

        childElem = parentElem.appendChild(newChildElem);
      }

      // Remove previous empty text element
      if (childElem.innerHTML === '' && getClassName(childElem.previousElementSibling) === textClass) {
        let parentElem = childElem.parentElement;
        parentElem.removeChild(childElem);
        childElem = parentElem.lastElementChild;
      }

      // Insert text to the current text element
      childElem.innerHTML += symbols;
    }

    return rootElem;
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
   * @param {string} indexParentClass - The parent CSS class of the index operator.
   * @param {string} indexClass - The CSS class of the index operator.
   * @returns {HTMLSpanElement} New child element.
   */
  function insertIndex(elem, indexParentClass, indexClass) {
    const indexParentElem = createElement(indexParentClass);

    const indexElem = createElement(indexClass);
    indexParentElem.appendChild(indexElem);

    if (indexChildClasses.includes(getClassName(elem))) {
      elem = elem.parentElement.parentElement;
    }

    elem.appendChild(indexParentElem);

    return indexElem;
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

    if (indexChildClasses.includes(getClassName(elem))) {
      elem = elem.parentElement;

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
    const closeBracketPos = getCloseBracketPos(mathText, start, end);

    if (closeBracketPos != -1) {
      let bracketsChildElem = toHtmlRec(mathText, start + 1, closeBracketPos - 1);

      if (funcName !== '') {
        bracketsChildElem = createFunctionElement(funcName, bracketsChildElem);
      }

      if (closeBracketPos < end && mathText[closeBracketPos + 1] === divOperator) {
        childElem = rootElem.appendChild(bracketsChildElem);
      } else if (indexChildClasses.includes(getClassName(childElem)) || getClassName(childElem) === denominatorClass) {
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

      childElem = rootElem.appendChild(createSvg(bracketPrefixClass));
    }

    return { start, childElem };

    //---------------------------------------------------------------------------------------------------------//

    /**
     * Put the given element in brackets.
     *
     * @param {HTMLSpanElement} elem - The element to put in brackets.
     */
    function putInBrackets(elem) {
      elem.insertBefore(createSvg(bracketPrefixClass), elem.firstElementChild);
      elem.appendChild(createSvg(bracketPostfixClass));
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

          let rootContentElem = tokenElems.length > 0 ? tokenElems[0] : createElement();
          setClassName(rootContentElem, rootContentClass);
          rootContentElem.style.borderColor = getColorWithOpacity(mathTextView.style.color, linesOpacity);

          let rootIndexElem = tokenElems.length > 1 ? tokenElems[1] : createElement();
          setClassName(rootIndexElem, rootIndexClass);
          rootIndexElem.style.borderColor = getColorWithOpacity(mathTextView.style.color, linesOpacity);

          elem = createElement(rootClass);
          elem.appendChild(rootIndexElem);
          elem.appendChild(createSvg(rootPrefixClass));
          elem.appendChild(rootContentElem);

          break;
        }
        case logFunction: {
          let tokenElems = tokenizeByComma(elem);

          let subElem = tokenElems.length > 0 ? tokenElems[0] : createElement();
          setClassName(subElem, subClass);

          let logContentElem = tokenElems.length > 1 ? tokenElems[1] : createElement();
          setClassName(logContentElem, logContentClass);

          let logIndexElem = createElement(logIndexClass);
          logIndexElem.appendChild(subElem);

          elem = createElement(logClass);
          elem.appendChild(logIndexElem, elem.firstElementChild);
          elem.appendChild(createSvg(bracketPrefixClass));
          elem.insertBefore(createFunctionNameElement(funcName), elem.firstElementChild);
          elem.appendChild(createSvg(bracketPostfixClass));
          elem.insertBefore(logContentElem, elem.lastElementChild);

          break;
        }
        case fracFunction: {
          let tokenElems = tokenizeByComma(elem);

          if (tokenElems.length > 2) {
            let integerElem = tokenElems[0];

            let numeratorElem = tokenElems[1];
            setClassName(numeratorElem, numeratorClass);

            let denominatorElem = tokenElems[2];
            setClassName(denominatorElem, denominatorClass);

            let fracElem = createElement(fractionClass);
            fracElem.appendChild(numeratorElem);
            fracElem.appendChild(denominatorElem);

            elem = createElement();
            insertChildren(elem, integerElem.children);
            elem.appendChild(fracElem);

            isSpecialFunc = false;
          } else {
            let numeratorElem = tokenElems.length > 0 ? tokenElems[0] : createElement();
            setClassName(numeratorElem, numeratorClass);

            let denominatorElem = tokenElems.length > 1 ? tokenElems[1] : createElement();
            setClassName(denominatorElem, denominatorClass);

            elem = createElement(fractionClass);
            elem.appendChild(numeratorElem);
            elem.appendChild(denominatorElem);
          }

          break;
        }
        case absFunction: {
          elem.insertBefore(createSvg(absPrefixClass), elem.firstChild);
          elem.appendChild(createSvg(absPostfixClass));

          isSpecialFunc = false;
          break;
        }
        case floorFunction: {
          elem.insertBefore(createFloorCeilElement(floorPrefixClass), elem.firstChild);
          elem.appendChild(createFloorCeilElement(floorPostfixClass));

          isSpecialFunc = false;
          break;
        }
        case ceilFunction: {
          elem.insertBefore(createFloorCeilElement(ceilPrefixClass), elem.firstChild);
          elem.appendChild(createFloorCeilElement(ceilPostfixClass));

          isSpecialFunc = false;
          break;
        }
        case derivativeFunction: {
          let tokenElems = tokenizeByComma(elem);

          let derivativeContentElem = tokenElems.length > 0 ? tokenElems[0] : createElement();
          setClassName(derivativeContentElem, derivativeContentClass);

          let derivativeBaseElem = tokenElems.length > 1 ? tokenElems[1] : createElement();
          setClassName(derivativeBaseElem, derivativeBaseClass);

          let derivativePrefixNumeratorElem = createElement(derivativeNumeratorClass);
          derivativePrefixNumeratorElem.appendChild(createSvg(derivativeNameClass));

          let derivativePrefixDenominatorElem = createElement(derivativeDenominatorClass);
          derivativePrefixDenominatorElem.appendChild(createSvg(derivativeNameClass));
          derivativePrefixDenominatorElem.appendChild(derivativeBaseElem);

          let derivativeFracElem = createElement(derivativeFracClass);
          derivativeFracElem.appendChild(derivativePrefixNumeratorElem);
          derivativeFracElem.appendChild(derivativePrefixDenominatorElem);

          elem = createElement(derivativeClass);
          elem.appendChild(derivativeFracElem);
          elem.appendChild(createSvg(bracketPrefixClass));
          elem.appendChild(derivativeContentElem);
          elem.appendChild(createSvg(bracketPostfixClass));

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
     * Create a prefix or postfix element of floor or ceil.
     *
     * @param {string} funcName - The element class name.
     * @returns {HTMLSpanElement} New ceil or floor element.
     */
    function createFloorCeilElement(className) {
      const elem = createElement(className);

      switch (className) {
        case floorPrefixClass: {
          elem.appendChild(createSvg(floorPrefixVerticalClass));
          elem.appendChild(createSvg(floorPrefixHorizontalClass));
          break;
        }
        case floorPostfixClass: {
          elem.appendChild(createSvg(floorPostfixHorizontalClass));
          elem.appendChild(createSvg(floorPostfixVerticalClass));
          break;
        }
        case ceilPrefixClass: {
          elem.appendChild(createSvg(ceilPrefixVerticalClass));
          elem.appendChild(createSvg(ceilPrefixHorizontalClass));
          break;
        }
        case ceilPostfixClass: {
          elem.appendChild(createSvg(ceilPostfixHorizontalClass));
          elem.appendChild(createSvg(ceilPostfixVerticalClass));
          break;
        }
      }

      return elem;
    }

    /**
     *
     * @param {HTMLSpanElement} elem - The element to tokenize.
     * @returns {HTMLSpanElement[]}
     */
    function tokenizeByComma(elem) {
      let lastTokenElem = createElement();
      const tokenElems = [lastTokenElem];

      while (elem.childElementCount > 0) {
        const childElem = elem.firstElementChild;

        if (childElem.getAttribute(beforeContentAttr) === comma) {
          lastTokenElem = createElement();
          tokenElems.push(lastTokenElem);
          elem.removeChild(childElem);
        } else {
          lastTokenElem.appendChild(childElem);
        }
      }

      return tokenElems;
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
    const openBracketCountMap = {
      [openBracket]: 0,
      [openSquareBracket]: 0,
    };
    const oppositeBracketMapReversed = {
      [closeBracket]: openBracket,
      [closeSquareBracket]: openSquareBracket,
    };

    const openBracketToSearch = mathText[start];

    for (let i = start; i <= end; i++) {
      const ch = mathText[i];

      if (ch in openBracketCountMap) {
        openBracketCountMap[ch]++;
      } else if (ch in oppositeBracketMapReversed) {
        openBracketCountMap[oppositeBracketMapReversed[ch]]--;

        if (oppositeBracketMapReversed[ch] === openBracketToSearch) {
          if (
            (ch === closeSquareBracket && openBracketCountMap[openBracketToSearch] === 0) ||
            !containsNonZero(openBracketCountMap)
          ) {
            return i;
          }
        }
      }
    }

    return -1;

    //---------------------------------------------------------------------------------------------------------//

    /**
     * Determine if the given map contains non-zero values.
     *
     * @param {Object} map - The map to check.
     * @returns {boolean} The result.
     */
    function containsNonZero(map) {
      let res = false;

      for (let key in map) {
        if (map[key] !== 0) {
          res = true;
          break;
        }
      }

      return res;
    }
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
      case bracketPrefixClass: {
        return openBracket;
      }
      case absPrefixClass: {
        return absFunction + openSquareBracket;
      }
      case floorPrefixClass: {
        return floorFunction + openSquareBracket;
      }
      case ceilPrefixClass: {
        return ceilFunction + openSquareBracket;
      }
      case bracketPostfixClass: {
        return closeBracket;
      }
      case absPostfixClass:
      case floorPostfixClass:
      case ceilPostfixClass: {
        return closeSquareBracket;
      }
      case functionNameClass: {
        return elem.getAttribute(beforeContentAttr);
      }
      case sqrtClass: {
        return sqrtFunction + putInSquareBrackets(toMathTextChildren(elem));
      }
      case rootClass: {
        return (
          rootFunction +
          putInSquareBrackets(
            toMathTextChildren(elem.children[2]) + comma + space + toMathTextChildren(elem.children[0])
          )
        );
      }
      case logClass: {
        return (
          logFunction +
          putInSquareBrackets(
            toMathTextChildren(elem.children[1]) + comma + space + toMathTextChildren(elem.children[3])
          )
        );
      }
      case fractionClass: {
        return (
          fracFunction +
          putInSquareBrackets(
            toMathTextChildren(elem.children[0]) + comma + space + toMathTextChildren(elem.children[1])
          )
        );
      }
      case derivativeClass: {
        return (
          derivativeFunction +
          putInSquareBrackets(
            toMathTextChildren(elem.children[2]) +
              comma +
              space +
              toMathTextChildren(elem.children[0].children[1].children[1])
          )
        );
      }
      case supParentClass: {
        return supOperator + putInSquareBrackets(toMathTextChildren(elem));
      }
      case subParentClass: {
        return subOperator + putInSquareBrackets(toMathTextChildren(elem));
      }
      case textClass: {
        return elem.innerText;
      }
      case piClass: {
        return piConst;
      }
      case eClass: {
        return eConst;
      }
      case iClass: {
        return iConst;
      }
      case infClass: {
        return infConst;
      }
      case complexInfClass: {
        return complexInfConst;
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

      const nextChildElem = childElem.nextElementSibling;

      if (
        !childContainerClasses.includes(getClassName(childElem)) &&
        !indexParentClasses.includes(getClassName(childElem)) &&
        !(getClassName(childElem) in bracketMapReversed) &&
        getClassName(childElem) !== unaryPostfixOperatorClass &&
        getClassName(prevChildElem) !== undefinedClass &&
        getClassName(prevChildElem) !== unaryPrefixOperatorClass &&
        getClassName(prevChildElem) !== functionNameClass &&
        !(getClassName(prevChildElem) in bracketMap)
      ) {
        text += space;
      }

      if (isNumberElement(childElem) && isNumberFractionElement(nextChildElem)) {
        text +=
          fracFunction +
          putInSquareBrackets(
            toMathTextRec(childElem) +
              comma +
              space +
              toMathTextChildren(nextChildElem.children[0]) +
              comma +
              space +
              toMathTextChildren(nextChildElem.children[1])
          );

        i++;
        prevChildElem = nextChildElem;
        continue;
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
  function putInSquareBrackets(text) {
    return openSquareBracket + text + closeSquareBracket;
  }
}

/**
 * Redraw all SVG sub children in the given element.
 *
 * @param {HTMLSpanElement} elem - The element to search.
 */
function redrawSvgs(elem) {
  initSvgsRec(elem);
  setBracketHeightsRec(elem);

  //---------------------------------------------------------------------------------------------------------//

  /**
   * Set color and init size of all SVG sub children in the given element.
   *
   * @param {HTMLSpanElement} elem - The element to search.
   */
  function initSvgsRec(elem) {
    if (elem.childElementCount === 0 || elem instanceof SVGElement) {
      return;
    }

    /** @type {(() => void)[]} */
    const writeCallbacks = [() => {}];

    for (let i = 0; i < elem.childElementCount; i++) {
      const childElem = elem.children[i];

      if (childElem instanceof SVGSVGElement) {
        writeCallbacks.push(() => setSvgColor(childElem));
      }

      if (getClassName(childElem) in bracketMap || getClassName(childElem) in bracketMapReversed) {
        writeCallbacks.push(() => setBracketHeight(makeBracketObject(childElem), null, true));
      }

      initSvgsRec(childElem);
    }

    for (let callback of writeCallbacks) {
      callback();
    }
  }

  /**
   * Set size of all bracket sub children in the given element.
   *
   * @param {HTMLSpanElement} elem - The element to search.
   */
  function setBracketHeightsRec(elem) {
    if (elem.childElementCount === 0 || elem instanceof SVGElement) {
      return;
    }

    const wasTextElemAdded = !textClasses.includes(getClassName(elem.firstElementChild));
    if (wasTextElemAdded) {
      elem.insertBefore(createElement(textClass), elem.firstElementChild);
    }

    const bracketMaxHeightStack = [makeHeightObject(elem.firstElementChild)];

    /** @type {SVGSVGElement[]} */
    const openBracketElemsStack = [];

    const children = getChildren(elem);

    /** @type {(() => void)[]} */
    const writeCallbacks = [() => {}];

    for (let childElem of children) {
      const className = getClassName(childElem);

      if (className === borderClass) {
        continue;
      }

      setBracketHeightsRec(childElem);

      if (bracketPrefixContainerClasses.includes(className)) {
        continue;
      }

      switch (className) {
        case bracketPrefixClass:
        case absPrefixClass:
        case floorPrefixClass:
        case ceilPrefixClass: {
          openBracketElemsStack.push(childElem);
          bracketMaxHeightStack.push(makeHeightObject(elem.firstElementChild));
          continue;
        }
        case bracketPostfixClass:
        case absPostfixClass:
        case floorPostfixClass:
        case ceilPostfixClass: {
          const heightObj = bracketMaxHeightStack[bracketMaxHeightStack.length - 1];
          const closeBracketObj = makeBracketObject(childElem);

          writeCallbacks.push(() => setBracketHeight(closeBracketObj, heightObj));

          if (openBracketElemsStack.length > 0) {
            const openBracketObj = makeBracketObject(openBracketElemsStack.pop());

            writeCallbacks.push(() => setBracketHeight(openBracketObj, heightObj));

            popHeightStack(bracketMaxHeightStack);
          } else {
            updateHeightStack(bracketMaxHeightStack, heightObj.bottom, heightObj.top, true);
          }

          continue;
        }
      }

      const useScale = bracketParentContainerClasses.includes(className);

      updateHeightStack(bracketMaxHeightStack, getElementBottom(childElem), getElementTop(childElem), useScale);
    }

    while (openBracketElemsStack.length > 0) {
      const bracketObj = makeBracketObject(openBracketElemsStack.pop());

      writeCallbacks.push(() => setBracketHeight(bracketObj, popHeightStack(bracketMaxHeightStack)));
    }

    for (let callback of writeCallbacks) {
      callback();
    }

    if (wasTextElemAdded) {
      elem.removeChild(elem.firstChild);
    }
  }

  /**
   * Pop the height stack and updates it.
   *
   * @param {{height: number; bottom: number; top: number;}[]} maxHeightStack - The height stack to pop.
   * @returns {{height: number; bottom: number; top: number;}} The popped height object.
   */
  function popHeightStack(maxHeightStack) {
    const heightObj = maxHeightStack.pop();
    updateHeightStack(maxHeightStack, heightObj.bottom, heightObj.top, true);
    return heightObj;
  }

  /**
   * Update the height stack with the given height, bottom and top.
   *
   * @param {{height: number; bottom: HTMLSpanElement; top: HTMLSpanElement;}[]} maxHeightStack - The height stack to update.
   * @param {number} bottom - The maximum bottom value.
   * @param {number} top - The minimum top value.
   * @param {boolean} useScale - Whether to use bracket scale.
   */
  function updateHeightStack(maxHeightStack, bottom, top, useScale = false) {
    const lastHeightObj = maxHeightStack[maxHeightStack.length - 1];

    const oldBottom = lastHeightObj.bottom;
    const newBottom = Math.max(oldBottom, bottom);

    const oldTop = lastHeightObj.top;
    const newTop = Math.min(oldTop, top);

    const oldHeight = lastHeightObj.height;
    let newHeight = Math.max(oldHeight, newBottom - newTop);

    if (useScale) {
      newHeight *= bracketNextScale;
    }

    if (oldHeight < newHeight) {
      maxHeightStack[maxHeightStack.length - 1] = { height: newHeight, bottom: newBottom, top: newTop };

      for (let i = maxHeightStack.length - 1; i > 0; i--) {
        if (maxHeightStack[i - 1].height < maxHeightStack[i].height) {
          maxHeightStack[i - 1] = maxHeightStack[i];
        }
      }
    }
  }

  /**
   * Set the bracket element height.
   *
   * @param {{element: SVGSVGElement; bottom: number; top: number;}} bracketObj - The bracket element to set its height.
   * @param {{height: number; bottom: number; top: number;}|null} heightObj - The height object.
   * @param {{height: number; bottom: number; top: number;}|null} heightObj - The height object.
   * @param {boolean} init - Whether to initialize element parameters.
   */
  function setBracketHeight(bracketObj, heightObj, init = false) {
    let bracketElem = bracketObj.element;

    switch (getClassName(bracketElem)) {
      case floorPrefixClass:
      case ceilPrefixClass: {
        bracketElem = bracketElem.firstElementChild;
        break;
      }
      case floorPostfixClass:
      case ceilPostfixClass: {
        bracketElem = bracketElem.lastElementChild;
        break;
      }
    }

    if (init) {
      bracketElem.setAttribute('preserveAspectRatio', 'none');
      bracketElem.style.verticalAlign = '';
      bracketElem.style.height = '';
      return;
    }

    const heightDelta = heightObj.height + bracketObj.top - bracketObj.bottom;
    const heightScaleDelta = (heightObj.height + heightObj.top - heightObj.bottom) / 2;

    if (heightObj.bottom < bracketObj.bottom) {
      bracketElem.style.verticalAlign = heightObj.bottom - bracketObj.bottom - heightDelta - heightScaleDelta + 'px';
    } else {
      bracketElem.style.verticalAlign = bracketObj.top - heightObj.top - heightDelta + heightScaleDelta + 'px';
    }

    bracketElem.style.height = heightObj.height + 'px';
  }

  /**
   * Return the element's children with sub children of function content containers.
   *
   * @param {HTMLSpanElement} elem - The element to get children.
   * @returns {HTMLSpanElement[]} The element's children.
   */
  function getChildren(elem) {
    let children = [];

    for (let i = 0; i < elem.childElementCount; i++) {
      let childElem = elem.children[i];

      if (bracketContentContainerClasses.includes(getClassName(childElem))) {
        children = children.concat(getChildren(childElem));
      } else {
        children.push(childElem);
      }
    }

    return children;
  }

  /**
   * Construct the height object from the given element.
   *
   * @param {HTMLSpanElement} elem - The given element.
   * @returns {{height: number; bottom: number; top: number;}} - New height object.
   */
  function makeHeightObject(elem) {
    const top = getElementTop(elem);
    const bottom = getElementBottom(elem);
    const height = bottom - top;
    return { height: height, bottom: bottom, top: top };
  }

  /**
   * Construct the bracket object from the given bracket element.
   *
   * @param {SVGSVGElement} bracketElem - The given bracket element.
   * @returns {{element: SVGSVGElement; bottom: number; top: number;}} - New bracket object.
   */
  function makeBracketObject(bracketElem) {
    return {
      element: bracketElem,
      bottom: getElementBottom(bracketElem),
      top: getElementTop(bracketElem),
    };
  }

  /**
   * Set the SVG icon color.
   *
   * @param {SVGSVGElement} elem - The SVG element to set its color.
   */
  function setSvgColor(elem) {
    let color;

    if (specialSvgClasses.includes(getClassName(elem))) {
      color = mathTextView.style.color;
    } else {
      color = getColorWithOpacity(mathTextView.style.color, linesOpacity);
    }

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
        indexParentClasses.includes(className) ||
        className in bracketMapReversed) &&
      (prevElemClassName === undefinedClass ||
        prevElemClassName === binaryOperatorClass ||
        prevElemClassName === unaryPrefixOperatorClass ||
        prevElemClassName in bracketMap)
    ) {
      elem.insertBefore(createElement(textHintClass), childElem);
      end++;
    }

    if (
      (className === binaryOperatorClass || className === unaryPrefixOperatorClass || className in bracketMap) &&
      (nextElemClassName === undefinedClass ||
        nextElemClassName === binaryOperatorClass ||
        nextElemClassName === unaryPostfixOperatorClass ||
        nextElemClassName in bracketMapReversed)
    ) {
      elem.insertBefore(createElement(textHintClass), childElem !== null ? childElem.nextElementSibling : null);
      end++;
      i++;
    }
  }
}

/**
 * Insert text hints to sub children in the given element.
 *
 * @param {HTMLSpanElement} elem - The element to insert text hints.
 */
function insertHintsRec(elem) {
  for (let i = 0; i < elem.childElementCount; i++) {
    const childElem = elem.children[i];
    insertHintsRec(childElem);
  }

  if (containerClasses.includes(getClassName(elem)) && elem.innerHTML === '') {
    elem.appendChild(createElement(textHintClass));
  }

  insertHints(elem);
}

/**
 * Insert empty text elements from the start child to the end child of the given element.
 *
 * @param {HTMLSpanElement} elem - The element to insert empty texts.
 * @param {number} start - The start index to check.
 * @param {number} end - The end index to check.
 */
function insertEmptyTexts(elem, start = 0, end = elem.childElementCount - 1) {
  if (start < 0 || start >= elem.childElementCount || parentContainerClasses.includes(getClassName(elem))) {
    return;
  }

  for (let i = start; i <= end; i++) {
    const childElem = elem.children[i];

    const className = getClassName(childElem);
    const prevElemClassName = childElem !== null ? getClassName(childElem.previousElementSibling) : undefinedClass;
    const nextElemClassName = childElem !== null ? getClassName(childElem.nextElementSibling) : undefinedClass;

    if (
      (containerClasses.includes(className) ||
        specialSvgClasses.includes(className) ||
        className in bracketMap ||
        className in bracketMapReversed ||
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
        specialSvgClasses.includes(className) ||
        className in bracketMap ||
        className in bracketMapReversed ||
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

/**
 * Insert empty texts to sub children in the given element.
 *
 * @param {HTMLSpanElement} elem - The element to insert empty texts.
 */
function insertEmptyTextsRec(elem) {
  for (let i = 0; i < elem.childElementCount; i++) {
    const childElem = elem.children[i];
    insertEmptyTextsRec(childElem);
  }

  insertEmptyTexts(elem);
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
    const childElemClassName = getClassName(childElem);

    if (
      (specialSvgClasses.includes(childElemClassName) ||
        childElemClassName in bracketMap ||
        operatorClasses.includes(childElemClassName)) &&
      isNotEmptyTextElement(childElem.nextElementSibling)
    ) {
      elem.insertBefore(createElement(borderClass), childElem.nextElementSibling);
    }

    if (
      (specialSvgClasses.includes(childElemClassName) ||
        childElemClassName in bracketMapReversed ||
        operatorClasses.includes(childElemClassName)) &&
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
      getClassName(prevElem) in bracketMap
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
        (getClassName(nextElem) !== binaryOperatorClass && !indexParentClasses.includes(getClassName(nextElem)))
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

  while (elem instanceof HTMLElement && elem.innerHTML !== elem.innerText && !isEmptyElement(elem)) {
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

  while (elem instanceof HTMLElement && elem.innerHTML !== elem.innerText && !isEmptyElement(elem)) {
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

    if (getClassName(childElem) === bracketPrefixClass) {
      openBracketsCount++;
    } else if (getClassName(childElem) === bracketPostfixClass) {
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
 * @param {HTMLSpanElement} elem - The element to check.
 * @returns {boolean} Whether the given element is a non empty text element.
 */
function isNotEmptyTextElement(elem) {
  return getClassName(elem) === textClass && elem.innerText !== '';
}

/**
 * Determine whether the given element is a empty element.
 *
 * @param {HTMLSpanElement} elem - The element to check.
 * @returns {boolean} Whether the given element is a empty element.
 */
function isEmptyElement(elem) {
  return (
    (textClasses.includes(getClassName(elem)) && elem.innerHTML === '') ||
    emptyElementClasses.includes(getClassName(elem))
  );
}

/**
 * Determine whether the given element is a text of a number.
 *
 * @param {HTMLSpanElement} elem - The element to check.
 * @returns {boolean} Whether the given element is a number element.
 */
function isNumberElement(elem) {
  return textClasses.includes(getClassName(elem)) && isNumber(elem.innerText);
}

/**
 * Determine whether the given element is a fraction of number elements.
 *
 * @param {HTMLSpanElement} elem - The element to check.
 * @returns {boolean} Whether the given element is a number fraction element.
 */
function isNumberFractionElement(elem) {
  if (getClassName(elem) !== fractionClass) {
    return false;
  }

  // TODO: improve performance
  const numeratorText = toMathText(elem.firstChild.innerHTML);
  const denominatorText = toMathText(elem.lastChild.innerHTML);
  return isNumber(numeratorText) && isNumber(denominatorText);
}

/**
 * Determine whether the given string is a natural number.
 *
 * @param {String} string - The string to check.
 * @returns {boolean} Whether the given string is a number.
 */
function isNumber(string) {
  return /^\d+$/.test(string);
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

/**
 * Return bottom + margin-bottom of the given element.
 *
 * @param {HTMLSpanElement} elem - The given element.
 * @returns {number} The element bottom.
 */
function getElementBottom(elem) {
  const style = window.getComputedStyle(elem);
  const marginBottom = parseFloatOrZero(style.marginBottom);
  return elem.getBoundingClientRect().bottom + marginBottom;
}

/**
 * Return top + margin-top of the given element.
 *
 * @param {HTMLSpanElement} elem - The given element.
 * @returns {number} The element top.
 */
function getElementTop(elem) {
  const style = window.getComputedStyle(elem);
  const marginTop = parseFloatOrZero(style.marginTop);
  return elem.getBoundingClientRect().top - marginTop;
}

/**
 * Convert RGB to RGBA color.
 *
 * @param {string} color - The RGB color string to convert.
 * @param {number} opacity - The opacity of a new RGBA color.
 * @returns {string} The RGBA color string.
 */
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
 * Convert a string to an integer.
 *
 * @param {String} string - The string to parse.
 * @returns {number} The result.
 */
function parseFloatOrZero(string) {
  return string !== '' ? parseFloat(string) : 0;
}

/**
 * Reverse the map object.
 *
 * @param {Object} mapObj - The map object to reverse.
 * @returns {Object} The reverse map object.
 */
function reverseMap(mapObj) {
  let res = {};

  Object.keys(mapObj).map((key) => {
    res[mapObj[key]] = key;
  });

  return res;
}

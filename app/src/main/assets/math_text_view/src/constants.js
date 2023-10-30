// Define a set of CSS class names that are used to style the math elements in the HTML.
const mathTextViewClass = 'math-text-view';
const undefinedClass = 'undefined';
const textClass = 'text';
const textHintClass = 'text-hint';
const borderClass = 'border';
const functionNameClass = 'function-name';
const binaryOperatorClass = 'binary-operator';
const unaryPrefixOperatorClass = 'unary-prefix-operator';
const unaryPostfixOperatorClass = 'unary-postfix-operator';
const openBracketClass = 'open-bracket';
const closeBracketClass = 'close-bracket';
const absPrefixClass = 'abs-prefix';
const absPostfixClass = 'abs-postfix';
const floorPrefixClass = 'floor-prefix';
const floorPostfixClass = 'floor-postfix';
const floorPrefixVerticalClass = 'floor-vertical-prefix';
const floorPostfixVerticalClass = 'floor-vertical-postfix';
const floorPrefixHorizontalClass = 'floor-horizontal-prefix';
const floorPostfixHorizontalClass = 'floor-horizontal-postfix';
const ceilPrefixClass = 'ceil-prefix';
const ceilPostfixClass = 'ceil-postfix';
const ceilPrefixVerticalClass = 'ceil-vertical-prefix';
const ceilPostfixVerticalClass = 'ceil-vertical-postfix';
const ceilPrefixHorizontalClass = 'ceil-horizontal-prefix';
const ceilPostfixHorizontalClass = 'ceil-horizontal-postfix';
const supClass = 'sup';
const subClass = 'sub';
const sqrtClass = 'sqrt';
const rootClass = 'root';
const rootContentClass = 'root-content';
const rootIndexClass = 'root-index';
const rootPrefixClass = 'root-prefix';
const logClass = 'log';
const logContentClass = 'log-content';
const logIndexClass = 'log-index';
const fractionClass = 'fraction';
const numeratorClass = 'numerator';
const denominatorClass = 'denominator';
const infClass = 'infinite';
const complexInfClass = 'complex-infinite';

// Define a set of CSS attributes.
const hintAttr = 'hint';
const beforeContentAttr = 'before-content';
const textDecorationAttr = 'underline dashed 0.05em';
const textDecorationNoneAttr = 'none';

// Define arrays of CSS class names to group related classes together.
const textClasses = [textClass, textHintClass];
const operatorClasses = [unaryPrefixOperatorClass, unaryPostfixOperatorClass, binaryOperatorClass];
const specialSvgClasses = [infClass, complexInfClass];
const parentContainerClasses = [
  fractionClass,
  sqrtClass,
  logClass,
  floorPrefixClass,
  floorPostfixClass,
  ceilPrefixClass,
  ceilPostfixClass,
];
const childContainerClasses = [
  numeratorClass,
  denominatorClass,
  rootContentClass,
  rootIndexClass,
  logContentClass,
  logIndexClass,
];
const indexContainerClasses = [supClass, subClass];
const containerClasses = parentContainerClasses + childContainerClasses + indexContainerClasses;

// Define constants for special symbols and strings.
const openBracket = '(';
const closeBracket = ')';
const divOperator = '/';
const supOperator = '^';
const subOperator = '_';
const modOperator = 'mod';
const absFunction = 'abs';
const floorFunction = 'floor';
const ceilFunction = 'ceil';
const sqrtFunction = 'sqrt';
const rootFunction = 'root';
const logFunction = 'log';
const fracFunction = 'frac';
const comma = ',';
const space = ' ';

// Define a mapping of mathematical strings to their corresponding HTML strings.
const mathHtmlMap = {
  '*': String.fromCodePoint(0x00d7),
  ' / ': String.fromCodePoint(0x00f7),
  '<=': String.fromCodePoint(0x2264),
  '>=': String.fromCodePoint(0x2265),
  '!=': String.fromCodePoint(0x2260),
  '&': String.fromCodePoint(0x2227),
  '|': String.fromCodePoint(0x2228),
  '~': String.fromCodePoint(0x00ac),
  '!<->': String.fromCodePoint(0x2262),
  '<->': String.fromCodePoint(0x2261),
  '->': String.fromCodePoint(0x21d2),
  '-': String.fromCodePoint(0x2212),
  deg: String.fromCodePoint(0x00b0),
  ComplexInf: String.fromCodePoint(0x29dd),
  Inf: String.fromCodePoint(0x221e),
  E: String.fromCodePoint(0x1d626),
  Pi: String.fromCodePoint(0x03c0),
  I: String.fromCodePoint(0x1d62a),
};

// Define a mapping for contentEditable=true of multi-character mathematical strings to their corresponding single-character HTML strings.
const mathEditableHtmlMap = {
  True: 'T',
  False: 'F',
};

// Define arrays of common binary, prefix, and postfix operators.
const binaryOperators = [
  '+',
  mathHtmlMap['-'],
  mathHtmlMap['*'],
  mathHtmlMap[' / '],
  '=',
  mathHtmlMap['!='],
  '>',
  '<',
  mathHtmlMap['<='],
  mathHtmlMap['>='],
  mathHtmlMap['&'],
  mathHtmlMap['|'],
  mathHtmlMap['->'],
  mathHtmlMap['<->'],
  mathHtmlMap['!<->'],
  comma,
  modOperator,
];
const unaryPrefixOperators = ['+', mathHtmlMap['-'], mathHtmlMap['~']];
const unaryPostfixOperators = ['%', '!', mathHtmlMap['deg']];

// Define array of special symbols to be displayed as an SVG.
const specialSvgSymbols = [mathHtmlMap['Inf'], mathHtmlMap['ComplexInf'], modOperator];

// Define a mapping for pairs like '(' and ')'.
const bracketMap = {
  [openBracketClass]: closeBracketClass,
  [absPrefixClass]: absPostfixClass,
  [floorPrefixClass]: floorPostfixClass,
  [ceilPrefixClass]: ceilPostfixClass,
};
const bracketMapReversed = reverseMap(bracketMap);

// Define constants for text styling.
const textEmptyHintSelected = '\u2B1A'; // TODO: use svg image instead
const linesOpacity = 0.75;
const textHintOpacity = 0.5;

// Define constants for SVG paths.
const openBracketSvgPath =
  'M43.256 0H57.14S14.95 25.902 15.086 83.25c.135 57.348 42.054 83.25 42.054 83.25H43.244S.366 138.573.002 83.25C-.362 27.927 43.256 0 43.256 0z';
const closeBracketSvgPath =
  'M13.883 0H0s42.19 25.902 42.054 83.25C41.92 140.598 0 166.5 0 166.5h13.896s42.877-27.927 43.241-83.25C57.502 27.927 13.883 0 13.883 0z';
const verticalLineSvgPath = 'M19.816564 0 h15 v166.5 h-15z';
const horizontalLineSvgPath = 'M-44.4 0v11.95H88.4V0z';
const sqrtPrefixSvgPath = 'M66.567 81.535l-4.56.003L14.993 9.987.008 9.904 0 .019 22.329 0l44.238 66.087z';
const infSvgPath =
  'M157.226 171.724c-25.653-3.105-36.571-18.532-43.457-28.85-6.886-10.317-5.067-7.675-23.498 13.36-18.43 21.036-43.719 15.107-50.898 13.69-7.18-1.418-22.736-11.555-30.485-26.86-7.749-15.304-10.622-36.6-3.562-59.373 7.059-22.772 35.3-44.18 60.994-36.669 25.694 7.511 30.43 17.725 37.917 26.583 7.489 8.859 8.476 8.734 12.553 1.692 4.076-7.041 12.435-25.435 39.393-28.99 26.957-3.555 57.45 12.563 60.156 60.81 2.705 48.246-33.46 67.711-59.113 64.607zm11.089-21.592c22.376-1.604 34.605-52.625 16.38-71.504-18.226-18.878-34.835-10.544-49.626 8.153-14.791 18.698-12.654 25.881-3.387 40.918 9.266 15.036 14.256 24.037 36.633 22.433zM87.94 124.314c8.115-13.107 8.403-18.457-5.566-41.06C68.406 60.653 35.468 58.18 27.1 90.147c-8.368 31.966 3.564 54.055 21.707 59.69 18.142 5.633 31.02-12.415 39.135-25.522z';
const complexInfSvgPath =
  'M157.226 171.724c-25.653-3.105-36.571-18.532-43.457-28.85-6.886-10.317-5.067-7.675-23.498 13.36-18.43 21.036-43.719 15.107-50.898 13.69-7.18-1.418-22.736-11.555-30.485-26.86-7.749-15.304-10.622-36.6-3.562-59.373 7.059-22.772 35.3-44.18 60.994-36.669 25.694 7.511 30.43 17.725 37.917 26.583 7.489 8.859 8.476 8.734 12.553 1.692 4.076-7.041 12.435-25.435 39.393-28.99 26.957-3.555 57.45 12.563 60.156 60.81 2.705 48.246-33.46 67.711-59.113 64.607zm11.089-21.592c22.376-1.604 34.605-52.625 16.38-71.504-18.226-18.878-34.835-10.544-49.626 8.153-14.791 18.698-12.654 25.881-3.387 40.918 9.266 15.036 14.256 24.037 36.633 22.433zM87.94 124.314c8.115-13.107 8.403-18.457-5.566-41.06C68.406 60.653 35.468 58.18 27.1 90.147c-8.368 31.966 3.564 54.055 21.707 59.69 18.142 5.633 31.02-12.415 39.135-25.522zm37.22-98.769c-16.04-2.957-25.803-7.16-35.53-9.938-9.727-2.78-19.18-.801-24.85 3.533-5.671 4.334-9.854 4.924-13.846 3.253-3.993-1.671 2.757-12.844 14.12-17.558C76.417.12 90.987.095 97.805 2.498c6.818 2.403 15.087 3.647 25.161 6.538 10.074 2.89 16.808 6.034 27.347-1.147 10.539-7.18 25.515-9.855 14.62 2.09-10.897 11.946-23.73 18.524-39.771 15.566z';

// Define constants for SVG view boxes.
const bracketSvgViewBox = '0 0 57 166';
const verticalLineSvgViewBox = '0 0 55 166';
const horizontalLineSvgViewBox = '0 0 44 12';
const sqrtPrefixViewBox = '0 0 43 81';
const infViewBox = '0 0 218 218';

// Define a mapping of class names to SVG elements.
const svgElementsMap = {
  [openBracketClass]: createNewSvg(openBracketClass, openBracketSvgPath, bracketSvgViewBox),
  [closeBracketClass]: createNewSvg(closeBracketClass, closeBracketSvgPath, bracketSvgViewBox),
  [absPrefixClass]: createNewSvg(absPrefixClass, verticalLineSvgPath, verticalLineSvgViewBox),
  [absPostfixClass]: createNewSvg(absPostfixClass, verticalLineSvgPath, verticalLineSvgViewBox),
  [floorPrefixVerticalClass]: createNewSvg(floorPrefixVerticalClass, verticalLineSvgPath, verticalLineSvgViewBox),
  [floorPostfixVerticalClass]: createNewSvg(floorPostfixVerticalClass, verticalLineSvgPath, verticalLineSvgViewBox),
  [ceilPrefixVerticalClass]: createNewSvg(ceilPrefixVerticalClass, verticalLineSvgPath, verticalLineSvgViewBox),
  [ceilPostfixVerticalClass]: createNewSvg(ceilPostfixVerticalClass, verticalLineSvgPath, verticalLineSvgViewBox),
  [floorPrefixHorizontalClass]: createNewSvg(
    floorPrefixHorizontalClass,
    horizontalLineSvgPath,
    horizontalLineSvgViewBox
  ),
  [floorPostfixHorizontalClass]: createNewSvg(
    floorPostfixHorizontalClass,
    horizontalLineSvgPath,
    horizontalLineSvgViewBox
  ),
  [ceilPrefixHorizontalClass]: createNewSvg(ceilPrefixHorizontalClass, horizontalLineSvgPath, horizontalLineSvgViewBox),
  [ceilPostfixHorizontalClass]: createNewSvg(
    ceilPostfixHorizontalClass,
    horizontalLineSvgPath,
    horizontalLineSvgViewBox
  ),
  [rootPrefixClass]: createNewSvg(rootPrefixClass, sqrtPrefixSvgPath, sqrtPrefixViewBox),
  [infClass]: createNewSvg(infClass, infSvgPath, infViewBox),
  [complexInfClass]: createNewSvg(complexInfClass, complexInfSvgPath, infViewBox),
};

// Define numeric coefficients
const bracketFirstScale = 1.175;
const bracketNextScale = 1.1;

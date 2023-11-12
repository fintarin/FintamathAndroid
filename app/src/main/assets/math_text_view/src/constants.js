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
const bracketPrefixClass = 'open-bracket';
const bracketPostfixClass = 'close-bracket';
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
const supParentClass = 'sup-parent';
const subParentClass = 'sub-parent';
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
const piClass = 'pi-const';
const eClass = 'e-const';
const iClass = 'i-const';
const infClass = 'inf-const';
const complexInfClass = 'complex-inf-const';

// Define a set of CSS attributes.
const hintAttr = 'hint';
const beforeContentAttr = 'before-content';
const textDecorationAttr = 'underline dashed 0.1ex';
const textDecorationNoneAttr = 'none';

// Define arrays of CSS class names to group related classes together.
const textClasses = [textClass, textHintClass];
const operatorClasses = [unaryPrefixOperatorClass, unaryPostfixOperatorClass, binaryOperatorClass];
const specialSvgClasses = [piClass, eClass, iClass, infClass, complexInfClass];
const indexParentClasses = [supParentClass, subParentClass];
const indexChildClasses = [supClass, subClass];
const functionContainerClasses = [logClass, logContentClass];
const parentContainerClasses = [
  fractionClass,
  rootClass,
  sqrtClass,
  logClass,
  logIndexClass,
  floorPrefixClass,
  floorPostfixClass,
  ceilPrefixClass,
  ceilPostfixClass,
].concat(indexParentClasses);
const childContainerClasses = [
  numeratorClass,
  denominatorClass,
  rootContentClass,
  rootIndexClass,
  logContentClass,
  logIndexClass,
].concat(indexChildClasses);
const containerClasses = parentContainerClasses.concat(childContainerClasses);
const bracketMap = {
  [bracketPrefixClass]: bracketPostfixClass,
  [absPrefixClass]: absPostfixClass,
  [floorPrefixClass]: floorPostfixClass,
  [ceilPrefixClass]: ceilPostfixClass,
};
const bracketMapReversed = reverseMap(bracketMap);

// Define constants for special symbols and strings.
const piConst = 'Pi';
const eConst = 'E';
const iConst = 'I';
const infConst = 'Inf';
const complexInfConst = 'ComplexInf';
const absFunction = 'abs';
const floorFunction = 'floor';
const ceilFunction = 'ceil';
const sqrtFunction = 'sqrt';
const rootFunction = 'root';
const logFunction = 'log';
const fracFunction = 'frac';
const openBracket = '(';
const closeBracket = ')';
const openSquareBracket = '[';
const closeSquareBracket = ']';
const divOperator = '/';
const supOperator = '^';
const subOperator = '_';
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
  'mod',
];
const unaryPrefixOperators = ['+', mathHtmlMap['-'], mathHtmlMap['~']];
const unaryPostfixOperators = ['%', '!', mathHtmlMap['deg']];

// Define constants for text styling.
const textEmptyHintSelected = '\u2B1A'; // TODO: use svg image instead
const linesOpacity = 0.75;
const textHintOpacity = 0.5;

// Define constants for SVG paths.
const openBracketSvgPath =
  'M43.256 0H57.14S14.95 25.902 15.086 83.25c.135 57.348 42.054 83.25 42.054 83.25H43.244S.366 138.573.002 83.25C-.362 27.927 43.256 0 43.256 0z';
const closeBracketSvgPath =
  'M13.883 0H0s40.556 25.902 40.42 83.25C40.286 140.598 0 166.5 0 166.5h13.896s40.994-28.126 41.358-83.449C55.619 27.728 13.883 0 13.883 0z';
const verticalLineSvgPath = 'M19.816564 0 h15 v166.5 h-15z';
const horizontalLineSvgPath = 'M-44.4 0v11.95H88.4V0z';
const sqrtPrefixSvgPath = 'M66.567 81.535l-4.56.003L14.993 9.987.008 9.904 0 .019 22.329 0l44.238 66.087z';
const piSvgPath =
  'M76.025 206.422c-5.426 19.988-30.308 9.935-24.231-3.58 11.325-25.19 33.326-76.01 42.592-110.61 0 0-27.42-2.33-34.103 0-10.094 3.358-20.408 18.4-28.894 25.636-3.734 3.184-9.943 1.076-8.179-3.503 3.688-9.57 23.146-34.879 37.463-42.242 6.88-3.54 14.24-4.778 40.413-4.516 35.401.354 86.153.026 99.207 0 9.413-.018 10.347 23.643 3.185 24.625-12.897 1.769-43.582 0-43.582 0s-2.433 11.566-4.592 25.777c-1.242 8.177-2.744 36.727-.522 48.037 6.032 30.71 13.142 28.902 11.541 40.645-1.575 11.553-22.291 16.178-26.935 3.803-5.493-14.637-14.677-37.29-.108-95.461.98-3.918 8.516-22.8 8.516-22.8h-36.998s-22.325 68.332-34.773 114.189z';
const eSvgPath =
  'M74.93 216.855c-33.703-6.391-21.892-85.252 18.865-125.95 31.298-31.253 79.086-22.722 68.058 12.458-5.669 18.083-16.286 31.756-65.03 53.77-6.921 3.126-8.008 1.245-9.295 11.418-4.174 33.003 10.854 32.774 55.723-1.069 3.038-2.291 10.228 10.447 7.337 13.119-28.145 26.008-57.76 39.647-75.657 36.254zm23.394-75.207c33.556-16.032 45.19-34.592 35.896-49.198-4.608-7.242-16.936-8.134-27.147 7.583-5.682 8.746-15.633 36.383-15.692 44.502-.003.376.891.004 6.943-2.887z';
const iSvgPath =
  'M33.26 209.626c-9.493-2.797-17.37-16.984-13.996-30.851 3.938-16.18 8.736-13.767 25.559-68.23 3.893-12.604 2.42-19.782-3.024-20.886-13.907-2.82-23.456 28.963-23.456 28.963l-13.61-1.96s4.683-16.428 11.018-26.435c12.614-19.924 28.465-21.154 40.751-14.2 9.232 5.224 14.298 19.6 8.363 38.734-16.845 54.298-29.795 71.41-19.692 77.125 12.862 7.274 25.886-30.15 25.886-30.15l12.046 1.64s-6.384 23.422-16.838 35.56c-10.454 12.138-23.514 13.487-33.007 10.69zM61.707 49.015c-12.586-3.53-9.458-24.437 4.136-27.641 9.948-2.346 16.893 6.135 13.416 16.38-2.705 7.971-10.789 13.157-17.552 11.26z';
const infSvgPath =
  'M157.226 171.724c-25.653-3.105-36.571-18.532-43.457-28.85-6.886-10.317-5.067-7.675-23.498 13.36-18.43 21.036-43.719 15.107-50.898 13.69-7.18-1.418-22.736-11.555-30.485-26.86-7.749-15.304-10.622-36.6-3.562-59.373 7.059-22.772 35.3-44.18 60.994-36.669 25.694 7.511 30.43 17.725 37.917 26.583 7.489 8.859 8.476 8.734 12.553 1.692 4.076-7.041 12.435-25.435 39.393-28.99 26.957-3.555 57.45 12.563 60.156 60.81 2.705 48.246-33.46 67.711-59.113 64.607zm11.089-21.592c22.376-1.604 34.605-52.625 16.38-71.504-18.226-18.878-34.835-10.544-49.626 8.153-14.791 18.698-12.654 25.881-3.387 40.918 9.266 15.036 14.256 24.037 36.633 22.433zM87.94 124.314c8.115-13.107 8.403-18.457-5.566-41.06C68.406 60.653 35.468 58.18 27.1 90.147c-8.368 31.966 3.564 54.055 21.707 59.69 18.142 5.633 31.02-12.415 39.135-25.522z';
const complexInfSvgPath =
  'M157.226 171.724c-25.653-3.105-36.571-18.532-43.457-28.85-6.886-10.317-5.067-7.675-23.498 13.36-18.43 21.036-43.719 15.107-50.898 13.69-7.18-1.418-22.736-11.555-30.485-26.86-7.749-15.304-10.622-36.6-3.562-59.373 7.059-22.772 35.3-44.18 60.994-36.669 25.694 7.511 30.43 17.725 37.917 26.583 7.489 8.859 8.476 8.734 12.553 1.692 4.076-7.041 12.435-25.435 39.393-28.99 26.957-3.555 57.45 12.563 60.156 60.81 2.705 48.246-33.46 67.711-59.113 64.607zm11.089-21.592c22.376-1.604 34.605-52.625 16.38-71.504-18.226-18.878-34.835-10.544-49.626 8.153-14.791 18.698-12.654 25.881-3.387 40.918 9.266 15.036 14.256 24.037 36.633 22.433zM87.94 124.314c8.115-13.107 8.403-18.457-5.566-41.06C68.406 60.653 35.468 58.18 27.1 90.147c-8.368 31.966 3.564 54.055 21.707 59.69 18.142 5.633 31.02-12.415 39.135-25.522zm37.22-98.769c-16.04-2.957-25.803-7.16-35.53-9.938-9.727-2.78-19.18-.801-24.85 3.533-5.671 4.334-9.854 4.924-13.846 3.253-3.993-1.671 2.757-12.844 14.12-17.558C76.417.12 90.987.095 97.805 2.498c6.818 2.403 15.087 3.647 25.161 6.538 10.074 2.89 16.808 6.034 27.347-1.147 10.539-7.18 25.515-9.855 14.62 2.09-10.897 11.946-23.73 18.524-39.771 15.566z';

// Define constants for SVG view boxes.
const bracketSvgViewBox = '0 0 57.1 166.5';
const verticalLineSvgViewBox = '0 0 55.7 166.5';
const horizontalLineSvgViewBox = '0 0 44 12';
const sqrtPrefixViewBox = '0 0 43 81';
const piViewBox = '22.9 67.6 185.3 150.3';
const eViewBox = '55.2 71.6 108.2 145.8';
const iViewBox = '4.7 21 78.4 190';
const infViewBox = '0 0 217.6 218.5';

// Define a mapping of class names to SVG elements.
const svgElementsMap = {
  [bracketPrefixClass]: createNewSvg(bracketPrefixClass, openBracketSvgPath, bracketSvgViewBox),
  [bracketPostfixClass]: createNewSvg(bracketPostfixClass, closeBracketSvgPath, bracketSvgViewBox),
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
  [piClass]: createNewSvg(piClass, piSvgPath, piViewBox),
  [eClass]: createNewSvg(eClass, eSvgPath, eViewBox),
  [iClass]: createNewSvg(iClass, iSvgPath, iViewBox),
  [infClass]: createNewSvg(infClass, infSvgPath, infViewBox),
  [complexInfClass]: createNewSvg(complexInfClass, complexInfSvgPath, infViewBox),
};

// Define numeric coefficients
const bracketNextScale = 1.1;

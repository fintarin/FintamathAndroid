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
const prefixAbsClass = 'abs-prefix';
const postfixAbsClass = 'abs-postfix';
const supClass = 'sup';
const subClass = 'sub';
const sqrtClass = 'sqrt';
const sqrtContentClass = 'sqrt-content';
const sqrtPrefixClass = 'sqrt-prefix';
const fractionClass = 'fraction';
const numeratorClass = 'numerator';
const denominatorClass = 'denominator';

// Define a set of CSS attributes.
const mathTextViewHintAttr = 'hint';
const emptyHintAttr = 'empty-hint';
const beforeContentAttr = 'before-content';
const textDecorationAttr = 'underline dashed 0.05em';
const textDecorationNoneAttr = 'none';

// Define arrays of CSS class names to group related classes together.
const textClasses = [textClass, textHintClass];
const operatorClasses = [unaryPrefixOperatorClass, unaryPostfixOperatorClass, binaryOperatorClass];
const bracketPrefixClasses = [openBracketClass, prefixAbsClass];
const bracketPostfixClasses = [closeBracketClass, postfixAbsClass];
const bracketClasses = bracketPrefixClasses + bracketPostfixClasses;
const parentContainerClasses = [fractionClass, sqrtClass];
const childContainerClasses = [numeratorClass, denominatorClass, sqrtContentClass];
const indexContainerClasses = [supClass, subClass];
const containerClasses = parentContainerClasses + childContainerClasses + indexContainerClasses;

// Define constants for special symbols and strings.
const openBracket = '(';
const closeBracket = ')';
const divOperator = '/';
const supOperator = '^';
const subOperator = '_';
const absFunction = 'abs';
const sqrtFunction = 'sqrt';
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
  Deg: String.fromCodePoint(0x00b0),
  E: String.fromCodePoint(0x1d626),
  Pi: String.fromCodePoint(0x03c0),
  I: String.fromCodePoint(0x1d456),
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
  ',',
];
const unaryPrefixOperators = ['+', mathHtmlMap['-'], mathHtmlMap['~']];
const unaryPostfixOperators = ['%', '!'];

// Define constants for text styling.
const textEmptyHintSelected = '\u2B1A'; // TODO: use svg image instead
const linesOpacity = 0.75;
const textHintOpacity = 0.5;

// Define constants for SVG paths.
const openBracketSvgPath =
  'M43.256 0H57.14S14.95 25.902 15.086 83.25c.135 57.348 42.054 83.25 42.054 83.25H43.244S.366 138.573.002 83.25C-.362 27.927 43.256 0 43.256 0z';
const closeBracketSvgPath =
  'M13.883 0H0s42.19 25.902 42.054 83.25C41.92 140.598 0 166.5 0 166.5h13.896s42.877-27.927 43.241-83.25C57.502 27.927 13.883 0 13.883 0z';
const absBorderSvgPath = 'm19.816564 0 h15 v166.5 h-15z';
const sqrtPrefixSvgPath = 'M66.567 81.535l-4.56.003L14.993 9.987.008 9.904 0 .019 22.329 0l44.238 66.087z';

// Define constants for SVG view boxes.
const bracketSvgViewBox = '0 0 57 166';
const absBorderSvgViewBox = '0 0 55 166';
const sqrtPrefixViewBox = '0 0 43 81';

// Define a mapping of class names to SVG elements.
const svgElementsMap = {
  [openBracketClass]: createNewSvg(openBracketClass, openBracketSvgPath, bracketSvgViewBox),
  [closeBracketClass]: createNewSvg(closeBracketClass, closeBracketSvgPath, bracketSvgViewBox),
  [prefixAbsClass]: createNewSvg(prefixAbsClass, absBorderSvgPath, absBorderSvgViewBox),
  [postfixAbsClass]: createNewSvg(postfixAbsClass, absBorderSvgPath, absBorderSvgViewBox),
  [sqrtPrefixClass]: createNewSvg(sqrtPrefixClass, sqrtPrefixSvgPath, sqrtPrefixViewBox),
};

// Define numeric coefficients
const bracketsScale = 0.1;

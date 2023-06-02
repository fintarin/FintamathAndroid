// Define a set of CSS class names that will be used to style the math elements in the HTML
const mathTextViewClass = 'math_text_view';
const textClass = 'text';
const textHintClass = 'text_hint';
const textBorderClass = 'border';
const functionClass = 'function';
const binaryOperatorClass = 'binary_operator';
const unaryPrefixOperatorClass = 'unary_prefix_operator';
const unaryPostfixOperatorClass = 'unary_postfix_operator';
const bracketsClass = 'brackets';
const absClass = 'abs';
const supClass = 'sup';
const subClass = 'sub';
const rootClass = 'root';
const fractionClass = 'fraction';
const fractionLineClass = 'fraction_line';
const numeratorClass = 'numerator';
const denominatorClass = 'denominator';

// Define arrays of CSS class names to group related classes together
const textClasses = [textClass, textHintClass, textBorderClass];
const supContainerClasses = [functionClass, fractionClass];
const containerClasses = [bracketsClass, absClass, rootClass];
const subContainerClasses = [numeratorClass, denominatorClass];
const indexContainerClasses = [supClass, subClass];
const operatorClasses = [unaryPrefixOperatorClass, unaryPostfixOperatorClass, binaryOperatorClass];

// Define a mapping of mathematical strings to their corresponding HTML strings
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
  '°': String.fromCodePoint(0x00b0),
  E: String.fromCodePoint(0x1d626),
  Pi: String.fromCodePoint(0x03c0),
  I: String.fromCodePoint(0x1d456),
};

// Define a mapping for contentEditable=true of large mathematical strings to their corresponding single-character HTML strings
const mathEditableHtmlMap = {
  True: 'T',
  False: 'F',
};

// Define arrays of common binary, prefix, and postfix operators
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
const unaryPostfixOperators = ['%', '!', mathHtmlMap['°']];

// Define constants for text styling
const textEmptyHintSelected = '\u2B1A';
const linesOpacity = 0.75;
const textHintOpacity = 0.5;

// 各値の正規表現
pattern {
	tag = /[^ #\/:]+/;
	name = /[^ #\/:]*/;
	mapkey = /[^ #\/:]+/;
}

// パスに使用する文字と解析用の正規表現
path {
	key = ':';
	level = '/';
	anchor = '#';
	upper = '..';
	decs = [
		/\/([^ #\/]+)/,
		/\/([^ #\/]+)\/([^ ]+)/
	]
	handles = [
		/([^ #\/]+)/,
		/([^ #\/]+)\/([^ ]+)/
	]
}

// スカラー値の解析に関する文字と正規表現
scalar {
	kwdNull = 'null';
	kwdTrue = 'true';
	kwdFalse = 'false';
	numInt = /-?\d+/;
	numBigDecimal = /-?\d+\.\d+/;
	refer = '@';
	rex = '~';
	str = '^';
}

// 文字列への変換用
tostr {
	decLevel = "!";
	handleLevel = '>';
	handleFormat = "#%s %s%s%s${System.lineSeparator()}";
	textEscape = "\t";
	textMeta = /\t*#.*/;
	listFormat = '#%s_%s';
	mapFormat = '#%s-%s%s';
	collecLevel = "\t";
	commentFormat = "# %s${System.lineSeparator()}";
}

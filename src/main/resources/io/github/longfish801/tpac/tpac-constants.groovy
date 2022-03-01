
// 行頭記号
bullet {
	dec = '#! '
	decEnd = '#!'
	handle = /^#(\d+)> /
	handleAlt = /^#(>{1,3}) /
	handleEnd = '#>'
	comment = '#:'
	map = '#-'
	textRange = /^#={3,}$/
	invalidText = '#'
}

// 各値の正規表現
value {
	key = /([^ #\/:]+:?[^ #\/:]*)/
	tag = /([^ #\/:]+)/
	name = /([^ #\/:]+)/
	scalar = /(.+)/
	mapkey = /([^ #\/:]+)/
}

// 各行の正規表現
line {
	dec = [
		/${bullet.dec}${value.key}/,
		/${bullet.dec}${value.key} ${value.scalar}/
	]
	handle = [
		/${bullet.handle}${value.key}/,
		/${bullet.handle}${value.key} ${value.scalar}/,
		/${bullet.handleAlt}${value.key}/,
		/${bullet.handleAlt}${value.key} ${value.scalar}/
	]
	key = [
		/${value.tag}:${value.name}/,
		/${value.tag}/
	]
	mapScalar = /${bullet.map}${value.mapkey} ${value.scalar}/
	mapText = /${bullet.map}${value.mapkey}/
}

// デフォルト値
dflt {
	handleName = 'dflt'
	mapKey = 'dflt'
}

// スカラー
scalar {
	kwdNull = 'null'
	kwdTrue = 'true'
	kwdFalse = 'false'
	numInt = /-?\d+/
	numBigDecimal = /-?\d+\.\d+/
	refer = '@'
	rex = ':'
	eval = '='
	str = '_'
	anchor = '#'
}

// パスに使用する文字と解析用の正規表現
path {
	keyDiv = ':'
	level = '/'
	anchor = '#'
	upper = '..'
	decs = [
		/\/([^ #\/]+)/,
		/\/([^ #\/]+)\/([^ ]+)/
	]
	handles = [
		/([^ #\/]+)/,
		/([^ #\/]+)\/([^ ]+)/
	]
}

// 文字列への変換用
tostr {
	handle = '#%s %s %s'
	handleNoScalar = '#%s %s'
	decleLevel = '!'
	handleLevel = '>'
	mapScalar = '#-%s %s'
	mapText = '#-%s'
	textRngDiv = '#==='
	textRngDivChar = '='
}

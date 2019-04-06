// 行頭記号
bol {
	dec = '#! ';
	decEnd = '#!!';
	handle = /#[0-9]*>{1,3} .+/;
	handleEnd = '#';
	text = '#';	// 否定で判定する
	textEscaped = /\t*#.*/;
	list = /#\t*_.*/;
	map = /#\t*-.+/;
	comment = '# ';
}

// 行頭記号を解析するための正規表現
bolPattern {
	handle1 = /([0-9]+)>/;
	handle2 = /(>{1,3})/;
}

// 各値の正規表現
pattern {
	handle = /([0-9]*>{1,3})/;
	tag = /([^ #\/:]+)/;
	name = /([^ #\/:]+)/;
	scalar = /(.+)/;
	mapkey = /([^ #\/:]+)/;
	collectionLevel = /(\t*)/;
}

// 各行の正規表現
patternLine {
	decs = [
		/#! ${pattern.tag}/,
		/#! ${pattern.tag} ${pattern.name}/,
		/#! ${pattern.tag} ${pattern.name} ${pattern.scalar}/
	];
	handles = [
		/#${pattern.handle} ${pattern.tag}/,
		/#${pattern.handle} ${pattern.tag} ${pattern.name}/,
		/#${pattern.handle} ${pattern.tag} ${pattern.name} ${pattern.scalar}/
	];
	list = [
		/#${pattern.collectionLevel}_${pattern.scalar}/,
		/#${pattern.collectionLevel}_/
	];
	map = [
		/#${pattern.collectionLevel}-${pattern.mapkey} ${pattern.scalar}/,
		/#${pattern.collectionLevel}-${pattern.mapkey}/
	];
	comment = /# (.*)/;
}


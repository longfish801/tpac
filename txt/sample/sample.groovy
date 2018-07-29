@GrabResolver(name = 'longfish801 github repositry', root = 'https://longfish801.github.io/maven/')
@Grab('io.github.longfish801:tpac:0.1.00')
@GrabExclude('org.codehaus.groovy:groovy-all')

import io.github.longfish801.tpac.Tpac;
import io.github.longfish801.tpac.parser.ParseException;

Tpac tpac = null;
try {
	tpac = new Tpac(new File('sample.tpac'));
	assert tpac.toXml() == new File('sample.xml').getText('UTF-8');
} catch (ParseException exc){
	println "文法誤りがあるため TPAC文書を解析できません。exc=${exc}";
}

@GrabResolver(name = 'longfish801 github repositry', root = 'https://longfish801.github.io/maven/')
@Grab('io.github.longfish801:tpac:0.2.00')
@GrabExclude('org.codehaus.groovy:groovy-all')

import io.github.longfish801.tpac.TeaServer;
import io.github.longfish801.tpac.TpacServer;
import io.github.longfish801.tpac.TeaServerParseException;
import io.github.longfish801.tpac.element.TeaDec;
import io.github.longfish801.tpac.element.TpacText;

try {
	// tpac文書を解析し、宣言を参照します
	TeaServer server = new TpacServer();
	server.soak(new File('account.tpac'));
	TeaDec dec = server['tpac:account'];
	
	// 読込内容を確認します
	assert dec.key == 'tpac:account';
	assert dec.comment.handle == [ '山田太郎のアカウントです。' ];
	assert dec.lowers['kind:personal'].lowers['elem:name'].scalar == '山田太郎';
	assert dec.lowers['kind:personal'].lowers['elem:age'].scalar == 19;
	assert dec.lowers['kind:personal'].lowers['map:family'].map == 
		[ 'father': '山田一郎', 'mother': '山田花子' ];
	assert dec.lowers['remarks:'].text == [ '編集中。', '後で見直します。' ] as TpacText;
	assert dec.lowers['guarantor:name'].scalar.refer() == '山田一郎';
	assert dec.lowers['hobby:'].list ==
		[ '国内旅行', [ 'Java', 'Groovy' ], [ '読書も好きです。', '推理小説をよく読みます。' ] as TpacText ];
	
} catch (TeaServerParseException exc){
	println "tpac文書の読込に失敗しました。exc=${exc}";
}

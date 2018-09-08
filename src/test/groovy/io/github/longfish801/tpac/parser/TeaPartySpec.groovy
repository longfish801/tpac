/*
 * TeaPartySpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.parser;

import groovy.util.logging.Slf4j;
import io.github.longfish801.shared.PackageDirectory;
import io.github.longfish801.tpac.TeaServer;
import io.github.longfish801.tpac.TpacServer;
import io.github.longfish801.tpac.element.TeaDec;
import io.github.longfish801.tpac.element.TpacText;
import spock.lang.Specification;
import spock.lang.Shared;

/**
 * TeaPartyクラスのテスト。
 * @version 1.0.00 2018/08/26
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TeaPartySpec extends Specification {
	/** ファイル入出力のテスト用フォルダ */
	static final File testDir = PackageDirectory.deepDir('src/test/resources', TeaPartySpec.class);
	/** TeaServer */
	@Shared TeaServer server;
	/** TeaParty */
	@Shared TeaParty teaParty;
	
	def setup(){
		server = new TpacServer();
		teaParty = new TeaParty(server);
	}
	
	def 'ファイルの内容をtpac文書とみなして解析します。'(){
		given:
		TeaDec dec;
		when:
		teaParty.soak(new File(testDir, 'account.tpac'));
		dec = server['tpac:account'];
		then:
		dec.key == 'tpac:account';
		dec.comment.handle == [ '山田太郎のアカウントです。' ];
		dec.lowers['kind:personal'].lowers['elem:name'].scalar == '山田太郎';
		dec.lowers['kind:personal'].lowers['elem:age'].scalar == 19;
		dec.lowers['kind:personal'].lowers['map:family'].map == 
			[ 'father': '山田一郎', 'mother': '山田花子' ];
		dec.lowers['remarks:'].text == [ '編集中。', '後で見直します。' ] as TpacText;
		dec.lowers['guarantor:name'].scalar.refer() == '山田一郎';
		dec.lowers['hobby:'].list ==
			[ '国内旅行', [ 'Java', 'Groovy' ], [ '読書も好きです。', '推理小説をよく読みます。' ] as TpacText ];
	}
	
	def 'URLの参照先をtpac文書とみなして解析します。'(){
		given:
		TeaDec dec;
		when:
		teaParty.soak(new File(testDir, 'account.tpac').toURI().toURL());
		dec = server['tpac:account'];
		then:
		dec.key == 'tpac:account';
		dec.comment.handle == [ '山田太郎のアカウントです。' ];
		dec.lowers['kind:personal'].lowers['elem:name'].scalar == '山田太郎';
		dec.lowers['kind:personal'].lowers['elem:age'].scalar == 19;
		dec.lowers['kind:personal'].lowers['map:family'].map == 
			[ 'father': '山田一郎', 'mother': '山田花子' ];
		dec.lowers['remarks:'].text == [ '編集中。', '後で見直します。' ] as TpacText;
		dec.lowers['guarantor:name'].scalar.refer() == '山田一郎';
		dec.lowers['hobby:'].list ==
			[ '国内旅行', [ 'Java', 'Groovy' ], [ '読書も好きです。', '推理小説をよく読みます。' ] as TpacText ];
	}
	
	def '宣言を解析します。'(){
		given:
		String source;
		
		when:
		source = '''\
			#! tpac
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:'].key == 'tpac:';
		
		when:
		source = '''\
			#! tpac hello
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:hello'].key == 'tpac:hello';
		
		when:
		source = '''\
			#! tpac bye hello
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:bye'].key == 'tpac:bye';
		server['tpac:bye'].scalar == 'hello';
		
		when:
		source = '''\
			BEFORE
			#! tpac goodbye hello
			#!!
			AFTER
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:goodbye'].key == 'tpac:goodbye';
		server['tpac:goodbye'].scalar == 'hello';
		
		when:
		source = '''\
			#! tpac key
			#! tpac word
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:key'].key == 'tpac:key';
		server['tpac:word'].key == 'tpac:word';
		
		when:
		source = '''\
			#! tpac yes
			#!!
			INNER
			#! tpac no
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:yes'].key == 'tpac:yes';
		server['tpac:no'].key == 'tpac:no';
	}
	
	def '宣言およびハンドルの親子関係を解析します。'(){
		given:
		String source;
		
		when:
		source = '''\
			#! tpac parent
			#> handle child
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:parent'].key == 'tpac:parent';
		server['tpac:parent'].lowers['handle:child'].key == 'handle:child';
		
		when:
		source = '''\
			#! tpac x
			#> handle y1
			#>> handle z
			#> handle y2
			#> handle y3
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:x'].key == 'tpac:x';
		server['tpac:x'].lowers['handle:y1'].key == 'handle:y1';
		server['tpac:x'].lowers['handle:y1'].lowers['handle:z'].key == 'handle:z';
		server['tpac:x'].lowers['handle:y2'].key == 'handle:y2';
		server['tpac:x'].lowers['handle:y3'].key == 'handle:y3';
	}
	
	def 'ハンドル開始行を解析します。'(){
		given:
		String source;
		
		when:
		source = '''\
			#! tpac hello
			#> handle
			#> handle bye
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:hello'].key == 'tpac:hello';
		server['tpac:hello'].lowers['handle:'].key == 'handle:';
		server['tpac:hello'].lowers['handle:bye'].key == 'handle:bye';
		
		when:
		source = '''\
			#! tpac here
			#> handle bye 21.5
			#> handle
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:here'].key == 'tpac:here';
		server['tpac:here'].lowers['handle:bye'].key == 'handle:bye';
		server['tpac:here'].lowers['handle:bye'].scalar == 21.5;
		server['tpac:here'].lowers['handle:'].key == 'handle:';
		
		when:
		source = '''\
			#! tpac hope
			#> handle chop
			#
			INLINE
			#>> handle stick
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:hope'].key == 'tpac:hope';
		server['tpac:hope'].lowers['handle:chop'].key == 'handle:chop';
		server['tpac:hope'].lowers['handle:chop'].lowers['handle:stick'].key == 'handle:stick';
	}
	
	def 'テキストを解析します。'(){
		given:
		String source;
		
		when:
		source = '''\
			#! tpac hello
			Hello, World!
			#> handle
			Then, Goodbye!
				# really?
					# is it true?
			It's a joke!
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:hello'].text.join() == 'Hello, World!';
		server['tpac:hello'].lowers['handle:'].text.join('|') == "Then, Goodbye!|# really?|\t# is it true?|It's a joke!";
	}
	
	def 'リストを解析します。'(){
		given:
		String source;
		
		when:
		source = '''\
			#! tpac hello
			#_a
			#_b
			#_c
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:hello'].list == [ 'a', 'b', 'c' ];
		
		when:
		source = '''\
			#! tpac bye
			#_a
			#_
			#	_ab
			#	_
			#		_abc
			#		_abd
			#_c
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:bye'].list == [ 'a', [ 'ab', [ 'abc', 'abd' ] ], 'c' ];
		
		when:
		source = '''\
			#! tpac yes
			#_a
			#_
			こんにちは。
			さようなら。
			#_
			#	_
			#		_
			さようなら。
			こんにちは。
			#_c
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:yes'].list == ['a', [ 'こんにちは。', 'さようなら。' ] as TpacText, [ [ [ 'さようなら。', 'こんにちは。' ] as TpacText ] ], 'c' ];
	}
	
	def 'マップを解析します。'(){
		given:
		String source;
		
		when:
		source = '''\
			#! tpac hello
			#-Yamada Taro
			#-Saito Hanako
			#-Suzuki Jiro
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:hello'].map == [ 'Yamada': 'Taro', 'Saito': 'Hanako', 'Suzuki': 'Jiro' ];
		
		when:
		source = '''\
			#! tpac bye
			#-some thing
			#-every
			#	-every thing
			#	-key
			#		-key word
			#		-key board
			#-some body
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:bye'].map == [ 'some': 'thing', 'every': [ 'every': 'thing', 'key': [ 'key': 'word', 'key': 'board' ] ], 'some': 'body' ];
		
		when:
		source = '''\
			#! tpac yes
			#-hobby programming
			#-note
			後で修正。
			追記すること。
			#-secret
			#	-top
			#		-secret
			実は、
			内緒。
			#-weight 60
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:yes'].map == [ 'hobby': 'programming', 'note': [ '後で修正。', '追記すること。' ] as TpacText, 'secret': [ 'top': [ 'secret': [ '実は、', '内緒。' ] as TpacText ] ], 'weight': 60 ];
	}
	
	def 'コメントを解析します。'(){
		given:
		String source;
		
		when:
		source = '''\
			#! tpac hello
			# 挨拶宣言です。
			#> handle bye
			# さよならハンドルです。
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:hello'].comment.handle == [ '挨拶宣言です。' ];
		server['tpac:hello'].lowers['handle:bye'].comment.handle == [ 'さよならハンドルです。' ];
		
		when:
		source = '''\
			#! tpac task
			#_電球
			#_トイレットペーパー
			# 買い物リストです。
			#-kitty 子猫
			#-dog 犬
			# 英単語マップです。
			今日も一日がんばろう。
			# 今週の標語です。
			'''.stripIndent();
		teaParty.soak(source);
		then:
		server['tpac:task'].comment.list == [ '買い物リストです。' ];
		server['tpac:task'].comment.map == [ '英単語マップです。' ];
		server['tpac:task'].comment.text == [ '今週の標語です。' ];
	}
}

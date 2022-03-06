/*
 * TpacPartySpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

import groovy.util.logging.Slf4j
import io.github.longfish801.gonfig.GropedResource
import io.github.longfish801.tpac.TpacConst as cnst
import io.github.longfish801.tpac.TpacMsg as msgs
import io.github.longfish801.tpac.tea.TeaParty
import spock.lang.Specification
import spock.lang.Shared

/**
 * TpacPartyクラスのテスト。
 * @version 0.3.00 2020/05/06
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacPartySpec extends Specification implements GropedResource {
	/** 自クラス */
	static final Class clazz = TpacPartySpec.class
	/** TpacParty */
	@Shared TpacParty party
	
	def setup(){
		party = new TpacParty(server: new TpacServer())
	}
	
	def 'parse - file'(){
		given:
		File deepDir = setBaseDir('src/test/resources').getDeepDir()
		List decs
		
		when:
		decs = party.parse(new File(deepDir, 'testfile.groovy'))
		then:
		decs.size() == 1
		decs[0].key == 'tpac:file'
	}
	
	def 'parse - url'(){
		given:
		List decs
		
		when:
		decs = party.parse(grope('testurl.groovy'))
		then:
		decs.size() == 1
		decs[0].key == 'tpac:url'
	}
	
	def 'parse - string'(){
		given:
		List decs
		String source
		
		when:
		source = '''\
			#! tpac:string
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs.size() == 1
		decs[0].key == 'tpac:string'
	}
	
	def 'parse'(){
		given:
		List decs
		String source
		
		when: '最小要素'
		source = '''\
			#! tpac
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs.size() == 1
		decs[0].key == 'tpac'
		
		when: '分岐の種類すべて'
		source = '''\
			#! tpac
			#> handle
			#===
			hello tpac!
			#===
			#-key1 hello
			#-key2
			hello2
			#>
			
			#> handle:some2
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs.size() == 1
		decs[0].key == 'tpac'
		decs[0].solve('handle').key == 'handle'
		decs[0].solve('handle').dflt == [ 'hello tpac!' ]
		decs[0].solve('handle').getAt('key1') == 'hello'
		decs[0].solve('handle').getAt('key2') == [ 'hello2' ]
		decs[0].solve('handle:some2').key == 'handle:some2'
	}
	
	def 'classifyLine'(){
		given:
		List decs
		String source
		
		when:
		source = '''\
			#! dec
			#> handle
			#:comment
			#-key1 val1
			#===
			text
			#invalid
			#===
			#-key2
			val2
			#>
			#!
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].solve('handle').key == 'handle'
		decs[0].solve('handle').comments == [ 'comment' ]
		decs[0].solve('handle').getAt('key1') == 'val1'
		decs[0].solve('handle').dflt == [ 'text', '#invalid' ]
		decs[0].solve('handle').getAt('key2') == [ 'val2' ]
	}
	
	def 'branchOut'(){
		given:
		List decs
		String source
		
		when: '文書の範囲外に宣言が現れたとき'
		source = '''\
			
			#! dec
			#!
			
			#! dec:some
			#!
			
			
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs.size() == 2
		decs[0].key == 'dec'
		decs[1].key == 'dec:some'
	}
	
	def 'branchHandle'(){
		given:
		List decs
		String source
		
		when: '宣言／ハンドル開始行からコメントまでの間に宣言が現れたとき'
		source = '''\
			#! dec
			#! dec:some
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs.size() == 2
		decs[0].key == 'dec'
		decs[1].key == 'dec:some'
		
		when: '宣言／ハンドル開始行からコメントまでの間に宣言終端が現れたとき'
		source = '''\
			#! dec
			#!
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		
		when: '宣言／ハンドル開始行からコメントまでの間にハンドル開始行が現れたとき'
		source = '''\
			#! dec
			#> handle
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].solve('handle').key == 'handle'
		
		when: '宣言／ハンドル開始行からコメントまでの間にハンドル終端が現れたとき'
		source = '''\
			#! dec
			#>
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		
		when: '宣言／ハンドル開始行からコメントまでの間にコメントが現れたとき'
		source = '''\
			#! dec
			#:comment
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].comments == [ 'comment' ]
		
		when: '宣言／ハンドル開始行からコメントまでの間にマップ スカラー値指定が現れたとき'
		source = '''\
			#! dec
			#-key hello
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].getAt('key') == 'hello'
		
		when: '宣言／ハンドル開始行からコメントまでの間にマップ テキスト指定が現れたとき'
		source = '''\
			#! dec
			#-key
			hello
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].getAt('key') == [ 'hello' ]
		
		when: '宣言／ハンドル開始行からコメントまでの間に明示的なテキスト範囲の開始が現れたとき'
		source = '''\
			#! dec
			#===
			hello
			#===
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].dflt == [ 'hello' ]
		
		when: '宣言／ハンドル開始行からコメントまでの間にテキストが現れたとき'
		source = '''\
			#! dec
			hello
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].dflt == [ 'hello' ]
	}
	
	def 'branchHandle - exception'(){
		given:
		String source
		TpacParseExceptions exc
		
		when: '宣言／ハンドル開始行からコメントまでの間に不正なテキストが現れたとき'
		source = '''\
			#! dec
			# invalid
			'''.stripIndent()
		party.parse(source)
		then:
		exc = thrown(TpacParseExceptions)
		exc.message == msgs.exc.parseError
		exc.errors[0].exc instanceof TpacSyntaxException
		exc.errors[0].exc.message == msgs.exc.noSharpTop
	}
	
	def 'branchMapScalar'(){
		given:
		List decs
		String source
		
		when: '宣言／ハンドルのコメントより後に宣言が現れたとき'
		source = '''\
			#! dec
			#-key hello
			#! dec:some
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs.size() == 2
		decs[0].key == 'dec'
		decs[0].getAt('key') == 'hello'
		decs[1].key == 'dec:some'
		
		when: '宣言／ハンドルのコメントより後に宣言終端が現れたとき'
		source = '''\
			#! dec
			#-key hello
			#!
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].getAt('key') == 'hello'
		
		when: '宣言／ハンドルのコメントより後にハンドル開始行が現れたとき'
		source = '''\
			#! dec
			#-key hello
			#> handle
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].getAt('key') == 'hello'
		decs[0].solve('handle').key == 'handle'
		
		when: '宣言／ハンドルのコメントより後にハンドル終端が現れたとき'
		source = '''\
			#! dec
			#-key hello
			#>
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].getAt('key') == 'hello'
		
		when: '宣言／ハンドルのコメントより後にマップ スカラー値指定が現れたとき'
		source = '''\
			#! dec
			#-key hello
			#-key2 hello2
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].getAt('key') == 'hello'
		decs[0].getAt('key2') == 'hello2'
		
		when: '宣言／ハンドルのコメントより後にマップ テキスト指定が現れたとき'
		source = '''\
			#! dec
			#-key hello
			#-key2
			hello2
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].getAt('key') == 'hello'
		decs[0].getAt('key2') == [ 'hello2' ]
		
		when: '宣言／ハンドルのコメントより後に明示的なテキスト範囲の開始が現れたとき'
		source = '''\
			#! dec
			#-key hello
			#===
			hello2
			#===
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].getAt('key') == 'hello'
		decs[0].dflt == [ 'hello2' ]
		
		when: '宣言／ハンドルのコメントより後にテキストが現れたとき'
		source = '''\
			#! dec
			#-key hello
			hello2
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].getAt('key') == 'hello'
		decs[0].dflt == [ 'hello2' ]
	}
	
	def 'branchMapScalar - exception'(){
		given:
		String source
		TpacParseExceptions exc
		
		when: '宣言／ハンドルのコメントより後にコメントが現れたとき'
		source = '''\
			#! dec
			#-key hello
			#:comment
			'''.stripIndent()
		party.parse(source)
		then:
		exc = thrown(TpacParseExceptions)
		exc.message == msgs.exc.parseError
		exc.errors[0].exc instanceof TpacSyntaxException
		exc.errors[0].exc.message == msgs.exc.commentBeforeMap
		
		when: '宣言／ハンドルのコメントより後に不正なテキストが現れたとき'
		source = '''\
			#! dec
			#-key hello
			# invalid
			'''.stripIndent()
		party.parse(source)
		then:
		exc = thrown(TpacParseExceptions)
		exc.message == msgs.exc.parseError
		exc.errors[0].exc instanceof TpacSyntaxException
		exc.errors[0].exc.message == msgs.exc.noSharpTop
	}
	
	def 'branchMapText'(){
		given:
		List decs
		String source
		
		when: 'マップの値がテキストで、テキストが現れたとき'
		source = '''\
			#! dec
			#-key
			hello
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].getAt('key') == [ 'hello' ]
		
		when: 'マップの値がテキストで、明示的なテキスト範囲の開始が現れたとき'
		source = '''\
			#! dec
			#-key
			#===
			hello
			#===
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].getAt('key') == [ 'hello' ]
	}
	
	def 'branchMapText - exception'(){
		given:
		String source
		TpacParseExceptions exc
		
		when:
		source = '''\
			#! dec
			#-key
			#-key2 hello
			'''.stripIndent()
		party.parse(source)
		then:
		exc = thrown(TpacParseExceptions)
		exc.message == msgs.exc.parseError
		exc.errors[0].exc instanceof TpacSyntaxException
		exc.errors[0].exc.message == msgs.exc.cannotBlankMapText
	}
	
	def 'branchText'(){
		given:
		List decs
		String source
		
		when: '明示的なテキスト範囲に終端あるいはテキストが現れたとき'
		source = '''\
			#! dec
			#===
			hello
			#===
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].dflt == [ 'hello' ]
	}
	
	def 'branchText - exception'(){
		given:
		String source
		TpacParseExceptions exc
		
		when:
		source = '''\
			#! dec
			#-vacant
			#===
			#===
			'''.stripIndent()
		party.parse(source)
		then:
		exc = thrown(TpacParseExceptions)
		exc.message == msgs.exc.parseError
		exc.errors[0].exc instanceof TpacSyntaxException
		exc.errors[0].exc.message == msgs.exc.cannotEmptyListText
	}
	
	def 'branchGap'(){
		given:
		List decs
		String source
		
		when: 'ハンドル間に宣言が現れたとき'
		source = '''\
			#! dec
			#>
			#! dec:some
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs.size() == 2
		decs[0].key == 'dec'
		decs[1].key == 'dec:some'
		
		when: 'ハンドル間にハンドル開始行が現れたとき'
		source = '''\
			#! dec
			#>
			#> handle
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].solve('handle').key == 'handle'
	}
	
	def 'leafDec'(){
		given:
		List decs
		String source
		
		when: 'ハンドル開始行にスカラー値なし'
		source = '''\
			#! dec
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].dflt == null
		
		when: 'ハンドル開始行にスカラー値あり'
		source = '''\
			#! dec hello
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].key == 'dec'
		decs[0].dflt == 'hello'
	}
	
	def 'leafDec - exception'(){
		given:
		String source
		TpacParseExceptions exc
		
		when:
		source = '''\
			#! dec/
			'''.stripIndent()
		party.parse(source)
		then:
		exc = thrown(TpacParseExceptions)
		exc.message == msgs.exc.parseError
		exc.errors[0].exc instanceof TpacSyntaxException
		exc.errors[0].exc.message == msgs.exc.invalidDec
	}
	
	def 'leafHandle'(){
		given:
		List decs
		String source
		
		when: 'ハンドル開始行にスカラー値なし'
		source = '''\
			#! dec
			#1> handle
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].solve('handle').key == 'handle'
		decs[0].solve('handle').dflt == null
		
		when: 'ハンドル開始行にスカラー値あり'
		source = '''\
			#! dec
			#1> handle hello
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].solve('handle').key == 'handle'
		decs[0].solve('handle').dflt == 'hello'
		
		when: 'ハンドル開始行が省略記法でスカラー値なし'
		source = '''\
			#! dec
			#> handle
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].solve('handle').key == 'handle'
		decs[0].solve('handle').dflt == null
		
		when: 'ハンドル開始行が省略記法でスカラー値あり'
		source = '''\
			#! dec
			#> handle hello
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].solve('handle').key == 'handle'
		decs[0].solve('handle').dflt == 'hello'
	}
	
	def 'leafHandle - exception'(){
		given:
		String source
		TpacParseExceptions exc
		
		when:
		source = '''\
			#! dec
			#1> handle/
			'''.stripIndent()
		party.parse(source)
		then:
		exc = thrown(TpacParseExceptions)
		exc.message == msgs.exc.parseError
		exc.errors[0].exc instanceof TpacSyntaxException
		exc.errors[0].exc.message == msgs.exc.invalidHandle
	}
	
	def 'leafComment'(){
		given:
		List decs
		String source
		
		when:
		source = '''\
			#! dec
			#:cmnt1
			#:cmnt2
			#:cmnt3
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].comments == [ 'cmnt1', 'cmnt2', 'cmnt3' ]
	}
	
	def 'leafMapScalar'(){
		given:
		List decs
		String source
		
		when:
		source = '''\
			#! dec
			#-key 123
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].getAt('key') == 123
	}
	
	def 'leafMapText'(){
		given:
		List decs
		String source
		
		when:
		source = '''\
			#! dec
			#-key
			hello1
			hello2
			hello3
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].getAt('key') == [ 'hello1', 'hello2', 'hello3' ]
	}
	
	def 'leafText'(){
		given:
		List decs
		String source
		
		when:
		source = '''\
			#! dec
			hello1
			hello2
			hello3
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].dflt == [ 'hello1', 'hello2', 'hello3' ]
		
		when: 'テキストが複数ある場合に行番号がリセットされること'
		source = '''\
			#! dec
			#-key1
			hello1
			hello2
			hello3
			#-key2
			bye1
			bye2
			bye3
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].getAt('key1') == [ 'hello1', 'hello2', 'hello3' ]
		decs[0].getAt('key2') == [ 'bye1', 'bye2', 'bye3' ]
	}
	
	def 'splitKey'(){
		given:
		List decs
		String source
		
		when: 'タグと名前の両方あり'
		source = '''\
			#! tag:name
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].tag == 'tag'
		decs[0].name == 'name'
		
		when: '名前を省略'
		source = '''\
			#! tag
			'''.stripIndent()
		decs = party.parse(source)
		then:
		decs[0].tag == 'tag'
		decs[0].name == cnst.dflt.handleName
	}
	
	def 'splitKey - exception'(){
		given:
		String source
		TpacParseExceptions exc
		
		when:
		source = '''\
			#! dec
			#1> handle:
			'''.stripIndent()
		party.parse(source)
		then:
		exc = thrown(TpacParseExceptions)
		exc.message == msgs.exc.parseError
		exc.errors[0].exc instanceof TpacSyntaxException
		exc.errors[0].exc.message == msgs.exc.invalidIdKey
	}
}

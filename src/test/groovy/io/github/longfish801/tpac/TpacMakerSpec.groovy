/*
 * TpacMakerSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac

import groovy.util.logging.Slf4j
import io.github.longfish801.tpac.TpacConst as cnst
import io.github.longfish801.tpac.TpacMsg as msgs
import java.util.regex.Pattern
import spock.lang.Specification
import spock.lang.Unroll
import spock.lang.Shared

/**
 * TpacMakerクラスのテスト。
 * @version 0.3.00 2020/05/06
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacMakerSpec extends Specification {
	/** TpacMaker */
	@Shared TpacMaker maker
	
	def setup(){
		maker = new TpacMaker()
	}
	
	def 'getDec'(){
		given:
		TpacDec dec
		
		when:
		dec = new TpacDec(tag: 'dec')
		maker.handle = dec
		then:
		maker.dec == dec
	}
	
	def 'newTeaDec'(){
		expect:
		maker.newTeaDec('tag', 'name') instanceof TpacDec
	}
	
	def 'newTeaHandle'(){
		expect:
		maker.newTeaHandle('tag', 'name', new TpacHandle()) instanceof TpacHandle
	}
	
	def 'createDec'(){
		when:
		maker.createDec('dec', 'some', 'hello')
		then:
		maker.handle.key == 'dec:some'
		maker.handle.dflt == 'hello'
		maker.keyForText == cnst.dflt.mapKey
	}
	
	def 'createHandle'(){
		when: 'ひとつ下の階層を追加'
		maker.createDec('dec', 'some', 'hello')
		maker.createHandle('handle', 'some', 1, 'bye')
		then:
		maker.handle.key == 'handle:some'
		maker.handle.upper.key == 'dec:some'
		maker.handle.dflt == 'bye'
		maker.keyForText == cnst.dflt.mapKey
		
		when: '同じ階層を連続して追加'
		maker.createDec('dec', 'some', 'hello')
		maker.createHandle('handle', 'some', 1, 'bye')
		maker.createHandle('handle', '', 1, 'hi')
		then:
		maker.handle.key == 'handle'
		maker.handle.upper.key == 'dec:some'
		maker.handle.dflt == 'hi'
		maker.keyForText == cnst.dflt.mapKey
		
		when: 'ひとつ上の階層を追加'
		maker.createDec('dec', 'some', 'hello')
		maker.createHandle('handle', '', 1, 'bye')
		maker.createHandle('handle', 'some', 2, 'hi')
		maker.createHandle('handle', 'buff', 1, 'bye-bye')
		then:
		maker.handle.key == 'handle:buff'
		maker.handle.upper.key == 'dec:some'
		maker.handle.dflt == 'bye-bye'
		maker.keyForText == cnst.dflt.mapKey
	}
	
	def 'createHandle - exception'(){
		given:
		TpacSemanticException exc
		
		when:
		maker.createDec('dec', 'some', 'hello')
		maker.createHandle('handle', 'some', 2, 'bye')
		then:
		exc = thrown(TpacSemanticException)
		exc.message == String.format(msgs.exc.cannotSkipLevel, 'handle', 'some', 2)
	}
	
	def 'createText'(){
		when: '１行目を格納'
		maker.createDec('dec', '', null)
		maker.keyForText = 'key'
		maker.createText('aaa', 1)
		then:
		maker.handle['key'] == [ 'aaa' ]
		
		when: '２行目を格納'
		maker.createDec('dec', '', null)
		maker.keyForText = 'key'
		maker.createText('aaa', 1)
		maker.createText('bbb', 2)
		then:
		maker.handle['key'] == [ 'aaa', 'bbb' ]
	}
	
	def 'createText - exception'(){
		given:
		TpacSemanticException exc
		
		when:
		maker.createDec('dec', '', null)
		maker.keyForText = 'key'
		maker.createText('aaa', 1)
		maker.createText('bbb', 1)
		then:
		exc = thrown(TpacSemanticException)
		exc.message == String.format(msgs.exc.duplicateMapKeyText, 'key')
	}
	
	def 'createMap'(){
		when: 'スカラー値が nullではない場合'
		maker.createDec('dec', '', null)
		maker.createMap('key', 'val')
		then:
		maker.handle['key'] == 'val'
		maker.keyForText == cnst.dflt.mapKey
		
		when: 'スカラー値が nullの場合'
		maker.createDec('dec', '', null)
		maker.createMap('key', null)
		then:
		maker.keyForText == 'key'
	}
	
	def 'createMap - exception'(){
		given:
		TpacSemanticException exc
		
		when:
		maker.createDec('dec', '', null)
		maker.createMap('key', 'val1')
		maker.createMap('key', 'val2')
		then:
		exc = thrown(TpacSemanticException)
		exc.message == String.format(msgs.exc.duplicateMapKey, 'key', 'val2')
	}
	
	def 'createComment'(){
		when:
		maker.createDec('dec', '', null)
		maker.createComment('this is comment')
		then:
		maker.handle.comments == [ 'this is comment' ]
	}
	
	@Unroll
	def 'evalScalar - unroll'(){
		given:
		maker.createDec('dec', '', null)
		
		expect:
		maker.evalScalar(raw) == expect
		
		where:
		raw		|| expect
		'null'	|| null
		'true'	|| true
		'false'	|| false
		'23'	|| 23
		'-1.05'	|| -1.05
		'_hello'	|| 'hello'
		'hello'	|| 'hello'
	}
	
	def 'evalScalar'(){
		given:
		maker.createDec('dec', '', null)
		def result
		
		when: '参照'
		result = maker.evalScalar('@/dec#key')
		then:
		result instanceof TpacRefer
		result.toString() == '/dec#key'
		
		when: '正規表現'
		result = maker.evalScalar(':.+')
		then:
		result instanceof Pattern
		result.pattern() == '.+'
		
		when: 'Groovy評価値'
		result = maker.evalScalar('=3+2')
		then:
		result instanceof TpacEval
		result.expression == '3+2'
	}
}

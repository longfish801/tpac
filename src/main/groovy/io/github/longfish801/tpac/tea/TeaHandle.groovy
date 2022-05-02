/*
 * TeaHandle.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.tea

import io.github.longfish801.tpac.TpacConst as cnst
import io.github.longfish801.tpac.TpacEval
import io.github.longfish801.tpac.TpacHandlingException
import io.github.longfish801.tpac.TpacMsg as msgs
import io.github.longfish801.tpac.TpacRefer
import io.github.longfish801.tpac.TpacSemanticException
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.slf4j.LoggerFactory

/**
 * ハンドルの特性です。<br/>
 * インスタンス生成後はタグ、上位ハンドルを設定してください。<br/>
 * 一部のメソッドで java.lang.NullpointerExceptionが発生する恐れがあります。
 * @author io.github.longfish801
 */
trait TeaHandle implements Cloneable {
	/** ログ出力 */
	private static final def LOG = LoggerFactory.getLogger(TeaHandle.class)
	/** タグ */
	String tag
	/** 名前 */
	String name = cnst.dflt.handleName
	/** 上位ハンドル */
	TeaHandle upper
	/** 識別キーと下位ハンドルとのマップ */
	Map<String, TeaHandle> lowers = [:]
	/** コメント */
	List comments = []
	/** マップ */
	Map map = [:]
	
	/**
	 * 識別キーを返します。<br/>
	 * 識別キーはタグ名と名前を半角コロンで連結した文字列です。<br/>
	 * 名前を省略した場合はタグ名のみを返します。
	 * @return 識別キー
	 */
	String getKey(){
		if (name == cnst.dflt.handleName) return tag
		return "${tag}${cnst.path.keyDiv}${name}" as String
	}
	
	/**
	 * 未加工の識別キーを返します。<br/>
	 * 識別キーはタグ名と名前を半角コロンで連結した文字列です。
	 * @return 未加工の識別キー
	 */
	String getKeyNatural(){
		return "${tag}${cnst.path.keyDiv}${name}" as String
	}
	
	/**
	 * このハンドルの階層を返します。
	 * @return 階層
	 */
	int getLevel(){
		return upper.level + 1
	}
	
	/**
	 * このハンドルの所属する宣言を返します。
	 * @return 宣言
	 */
	TeaDec getDec(){
		return upper.dec
	}
	
	/**
	 * 下位ハンドルを追加します。<br/>
	 * メンバ変数lowersに、未加工の識別キーをキーとして格納します。
	 * @param lower 下位ハンドル
	 * @return 自インスタンス
	 */
	TeaHandle leftShift(TeaHandle lower){
		lowers[lower.keyNatural] = lower
		lower.upper = this
		return this
	}
	
	/**
	 * マップからキーに対応する値を参照します。
	 * @param key キー
	 * @return キーに対応する値
	 */
	def getAt(String key){
		return map.get(key)
	}
	
	/**
	 * マップにキーとそれに対応する値を格納します。
	 * @param key キー
	 * @param val キーに対応する値
	 * @return 自インスタンス
	 */
	TeaHandle setAt(String key, def val){
		map.put(key, val)
		return this
	}
	
	/**
	 * マップからキーに対応する値を参照します。
	 * @param key キー
	 * @return キーに対応する値
	 */
	def propertyMissing(String key){
		return getAt(key)
	}
	
	/**
	 * マップにキーとそれに対応する値を格納します。
	 * @param key キー
	 * @param val キーに対応する値
	 * @return 自インスタンス
	 */
	TeaHandle propertyMissing(String key, def val){
		return setAt(key, val)
	}
	
	/**
	 * このハンドルの絶対パスを返します。
	 * @return 絶対パス
	 */
	String getPath(){
		return "${upper.path}${cnst.path.level}${key}" as String
	}
	
	/**
	 * パスに対応するハンドルを返します。<br/>
	 * 絶対パスの場合は宣言に解決を依頼します。<br/>
	 * 相対パスの場合、パス区切り文字で分割した先頭の識別キーと、残りのパスに分割します。<br/>
	 * このとき、統語的にありえないパスの場合は例外を投げます。<br/>
	 * 先頭の識別キーに応じて以下のとおり処理します。</p>
	 * <dl>
	 * <dt>先頭の識別キーが上位ハンドルの識別キーと一致する場合</dt>
	 * 	<dd>残りのパスがなければ上位ハンドルを返します。</dd>
	 * 	<dd>残りのパスがあれば、それを新たなパスとして上位ハンドルに依頼します。</dd>
	 * <dt>上記以外の場合</dt>
	 * 	<dd>先頭の識別キーにタグと名前の区切りがなければ、未加工の識別キーに修正します。</dd>
	 * 	<dd>残りのパスがなければ、未加工の識別キーが一致する下位ハンドルを返します。</dd>
	 * 	<dd>残りのパスがあれば、それを新たなパスとして未加工の識別キーが一致する下位ハンドルに依頼します。</dd>
	 * </dl>
	 * <p>上記で該当するハンドルがなければ nullを返します。
	 * @param path パス
	 * @return パスに対応するハンドル（該当するハンドルがなければnull）
	 * @exception TpacHandlingException 統語的にありえないパスです
	 */
	TeaHandle solve(String path){
		// 絶対パスの場合は宣言に解決を依頼します
		if (path.startsWith(cnst.path.level)) return dec.solve(path)
		// パス区切り文字で分割した先頭の要素を解決します
		if (cnst.path.handles.every { !(path ==~ it) }){
			throw new TpacHandlingException(String.format(msgs.exc.invalidpath, path))
		}
		Matcher matcher = Matcher.lastMatcher
		String firstPath = matcher.group(1)
		String otherPath = (matcher.groupCount() >= 2)? matcher.group(2) : ''
		if (firstPath == cnst.path.upper){	// 上位のパスの場合
			return (otherPath.empty)? upper : upper?.solve(otherPath)
		}
		// 下位ハンドルの場合
		if (firstPath.indexOf(cnst.path.keyDiv) < 0) firstPath = "${firstPath}${cnst.path.keyDiv}${cnst.dflt.handleName}"
		return (otherPath.empty)? lowers[firstPath] : lowers[firstPath]?.solve(otherPath)
	}
	
	/**
	 * パスに対応するハンドルあるいはマップの値を返します。<br/>
	 * パスにアンカーを含まない場合は、{@link #solve(String)}を返します。
	 * アンカーを含む場合、まずアンカーを除いたパスに相当するハンドルを参照します。
	 * アンカーを除いたパスが空文字列ならば自ハンドルです。
	 * アンカーを除いたパスが空文字列でなければ{@link #solve(String)}でハンドルを参照します。
	 * アンカーが空文字列のときはデフォルトキーとみなします。
	 * ハンドルのマップからキーと紐づく値を返します。
	 * @param path パス
	 * @return パスに対応するハンドルあるいはマップの値（該当するハンドルがなければnull）
	 */
	def refer(String path){
		// パスにアンカーを含まない場合は、{@link #solve(String)}を返します
		int idx = path.indexOf(cnst.path.anchor)
		if (idx < 0) return solve(path)
		// アンカーと、アンカーを含まないパスを取得します
		String anchor = path.substring(idx + 1)
		if (anchor.empty) anchor = cnst.dflt.mapKey
		path = path.substring(0, idx)
		// パスが示す対象を返します
		TeaHandle hndl = (path.empty)? this : solve(path)
		return hndl?.getAt(anchor)
	}
	
	/**
	 * 未加工の識別キーの一部が正規表現とマッチする下位ハンドルのリストを取得します。<br/>
	 * 直下の下位ハンドルのみ探します。<br/>
	 * 再帰的にさらに下位まで探すわけではないことに注意してください。
	 * @param regex 正規表現
	 * @return 未加工の識別キーが正規表現と一致する下位ハンドルのリスト（みつからない場合は空リスト）
	 */
	List<TeaHandle> findAll(String regex){
		return lowers.values().findAll { it.keyNatural =~ regex }
	}
	
	/**
	 * クロージャによって対象と判定した下位ハンドルのリストを取得します。<br/>
	 * クロージャには引数として未加工の識別キーを渡します。<br/>
	 * 戻り値に含めるか否か判定を返してください。<br/>
	 * 直下の下位ハンドルのみ探します。<br/>
	 * 再帰的にさらに下位まで探すわけではないことに注意してください。
	 * @param clos 未加工の識別キーから対象か否か判定を返すクロージャ
	 * @return クロージャが対象と判定した下位ハンドルのリスト（みつからない場合は空リスト）
	 */
	List<TeaHandle> findAll(Closure clos){
		return lowers.values().findAll { clos.call(it.keyNatural) }
	}
	
	/**
	 * キーの妥当性を検証します。<br/>
	 * キーの条件として以下のマップを指定してください。</p>
	 * <dl>
	 * <dt>キー</dt>
	 *   <dd>マップのキー</dd>
	 * <dt>値</dt>
	 *   <dd>キーの値が満たすべき条件を定義したマップ
	 *      <dl>
	 *      <dt>必須チェックをしたい場合はキー「required」にtrueを指定してください。</dt>
	 *        <dd>キーに値が指定されていなければ例外を投げます。</dd>
	 *      <dt>デフォルト値を指定したい場合はキー「dflt」にデフォルト値を指定してください。</dt>
	 *        <dd>キーに値が指定されていなければデフォルト値を格納します。</dd>
	 *      <dt>クラスのチェックをしたい場合はキー「types」には設定可能な値のクラスをリストで指定してください。</dt>
	 *        <dd>値がリストにないクラスであれば例外を投げます。</dd>
	 *      </dl>
	 *   </dd>
	 * </dl>
	 * <p>上記で例外とは {@link io.github.longfish801.tpac.TpacSemanticException}のことです。
	 * @param conds キーの条件
	 */
	void validateKeys(Map conds){
		conds.each { String condKey, Map condMap ->
			if (map.containsKey(condKey)){
				// 指定可能なクラスでなければ例外を投げます
				if (condMap.types instanceof List){
					if (condMap.types.every { !(it?.isInstance(map.get(condKey)) || (it == null && map.get(condKey) == null)) }){
						throw new TpacSemanticException(String.format(msgs.validate.invalidType, condKey, map.get(condKey)?.class?.name))
					}
				}
			} else {
				// 必須項目の値が指定されていない場合は例外を投げます
				if (condMap.required == true){
					throw new TpacSemanticException(String.format(msgs.validate.unspecifiedValue, condKey))
				}
				// デフォルト値を格納します
				if (condMap.containsKey('dflt')) map.put(condKey, condMap.dflt)
			}
		}
	}
	
	/**
	 * このハンドルの妥当性を検証します。<br/>
	 * このメソッドは {@link TeaServer#soak(def)}からハンドルを作成したときに呼びます。<br/>
	 * 必要に応じてオーバーライドし、妥当性の検証に利用してください。<br/>
	 * 問題があったときは {@link io.github.longfish801.tpac.TpacSemanticException}を投げてください。
	 * @see #validateRecursive()
	 */
	void validate(){
	}
	
	/**
	 * このハンドルならびに下位ハンドルの妥当性を検証します。
	 * 問題があったときは {@link io.github.longfish801.tpac.TpacSemanticException}を投げます。
	 * @see #validate()
	 * @return 自インスタンス
	 */
	TeaHandle validateRecursive(){
		// 自ハンドルの妥当性を検証します
		validate()
		// 下位ハンドルの妥当性を検証します
		lowers.values().each { it.validateRecursive() }
		return this
	}
	
	/**
	 * このハンドルに特有の処理をします。<br/>
	 * デフォルトでは特になにも処理をしません。<br/>
	 * 必要に応じてオーバーライドし、初期化などをしたい場合に利用してください。<br/>
	 * このメソッドは{@link #visitRecursive()}から呼ばれます。
	 */
	void visit(){
	}
	
	/**
	 * このハンドルならびに下位ハンドルに特有の処理をします。
	 * @see #visit()
	 * @return 自インスタンス
	 */
	TeaHandle visitRecursive(){
		// 自ハンドルの処理をします。
		visit()
		// 下位ハンドルの処理をします
		lowers.values().each { it.visitRecursive() }
		return this
	}
	
	/**
	 * 文字列表現を出力します。<br/>
	 * 以下の順番で出力します。</p>
	 * <ol>
	 * <li>ハンドル開始行</li>
	 * <li>コメント</li>
	 * <li>マップ スカラー値指定</li>
	 * <li>デフォルトキーのテキスト</li>
	 * <li>マップ テキスト指定</li>
	 * <li>ハンドル終端</li>
	 * <li>下位のハンドル（再帰的呼出）</li>
	 * </ol>
	 * @param writer Writer
	 */
	void write(Writer writer){
		// ハンドル開始行
		String bullet
		switch (level){
			case 0: bullet = cnst.tostr.decleLevel; break
			case 1..3: bullet = cnst.tostr.handleLevel * level; break
			default: bullet = "${level}${cnst.tostr.handleLevel}"
		}
		if (map.containsKey(cnst.dflt.mapKey) && !(map[cnst.dflt.mapKey] instanceof List)){
			writer << String.format(cnst.tostr.handle, bullet, key, formatScalar(map[cnst.dflt.mapKey]))
		} else {
			writer << String.format(cnst.tostr.handleNoScalar, bullet, key)
		}
		writer << System.lineSeparator()
		// コメント
		if (comments.size() > 0){
			writer << comments.collect { "${cnst.bullet.comment}${it}" }.join(System.lineSeparator())
			writer << System.lineSeparator()
		}
		// マップ スカラー値指定
		map.each { String key, def value ->
			if (key != cnst.dflt.mapKey && !(value instanceof List)){
				writer << String.format(cnst.tostr.mapScalar, key, formatScalar(value))
				writer << System.lineSeparator()
			}
		}
		// デフォルトキーのテキスト
		if (map.containsKey(cnst.dflt.mapKey) && map[cnst.dflt.mapKey] instanceof List){
			writer << formatText(map[cnst.dflt.mapKey])
			writer << System.lineSeparator()
		}
		// マップ テキスト指定
		map.each { String key, def value ->
			if (key != cnst.dflt.mapKey && value instanceof List){
				writer << String.format(cnst.tostr.mapText, key)
				writer << System.lineSeparator()
				writer << formatText(value)
				writer << System.lineSeparator()
			}
		}
		// ハンドル終端
		writer << cnst.bullet.handleEnd
		writer << System.lineSeparator()
		writer << System.lineSeparator()
		
		// 下位のハンドル
		if (!lowers.isEmpty()){
			lowers.values().each { it.write(writer) }
		}
	}
	
	/**
	 * テキストの文字列表現を返します。<br/>
	 * 先頭が半角シャープで始まる行がなければ、範囲を示す区切り行は省略します。<br/>
	 * 先頭が半角シャープで始まる行があれば、区切り行で範囲を明示します。<br/>
	 * 区切り行がすでに存在する場合、半角イコールをひとつ増やした区切り行ならば
	 * 問題ないか再帰的に確認することで、区切り行を決定します。
	 * @param lines テキスト
	 * @return テキストの文字列表現
	 */
	static String formatText(List lines){
		// 先頭が半角シャープで始まる行がなければ範囲を示す区切り行は省略します
		if (lines.every { !it.startsWith(cnst.bullet.invalidText) }){
			return lines.join(System.lineSeparator())
		}
		
		// 明示的なテキスト範囲の区切り行を決定するクロージャ
		Closure decideDiv
		decideDiv = { String curDiv ->
			if (lines.every { it != curDiv }) return curDiv
			return decideDiv("${curDiv}${cnst.tostr.textRngDivChar}")
		}
		
		// 範囲を示す区切り行で挟んだ文字列表現を返す
		String divLine = decideDiv(cnst.tostr.textRngDiv)
		lines.add(0, divLine)
		lines << divLine
		return lines.join(System.lineSeparator())
	}
	
	/**
	 * スカラー値を文字列表現に変換します。<br/>
	 * 他のデータ型に変換される恐れのある文字列は、
	 * 明示的に文字列を意味する文字列表現に変換します。
	 * @param value スカラー値
	 * @return スカラー値の文字列表現
	 * @exception TpacHandlingException 文字列表現への変換が想定されていないスカラー値です。
	 */
	static String formatScalar(def value){
		String raw
		switch (value){
			case null:
				raw = cnst.scalar.kwdNull
				break
			case true:
				raw = cnst.scalar.kwdTrue
				break
			case false:
				raw = cnst.scalar.kwdFalse
				break
			case Integer:
			case BigDecimal:
				raw = value.toString()
				break
			case TpacRefer:
				raw = "${cnst.scalar.refer}${value.toString()}"
				break
			case Pattern:
				raw = "${cnst.scalar.rex}${value.pattern()}"
				break
			case TpacEval:
				raw = "${cnst.scalar.eval}${value.expression}"
				break
			case GString:
				value = value.toString()
			case String:
				switch (value){
					case cnst.scalar.kwdNull:
					case cnst.scalar.kwdTrue:
					case cnst.scalar.kwdFalse:
					case {it ==~ cnst.scalar.numInt}:
					case {it ==~ cnst.scalar.numBigDecimal}:
					case {it.startsWith(cnst.scalar.refer)}:
					case {it.startsWith(cnst.scalar.rex)}:
					case {it.startsWith(cnst.scalar.eval)}:
					case {it.startsWith(cnst.scalar.str)}:
					case {cnst.scalar.esc.matcher(it).find()}:
					case {it.empty}:
						// Javaにおけるエスケープシーケンスの対象となる文字（ただしマルチバイト文字を除く）をエスケープします
						value = value.replaceAll('\\\\', '\\\\\\\\')
						value = value.replaceAll(/\n/, /\\n/)
						value = value.replaceAll(/\r/, /\\r/)
						value = value.replaceAll(/\f/, /\\f/)
						value = value.replaceAll(/\u0008/, /\\b/)
						value = value.replaceAll(/\t/, /\\t/)
						value = value.replaceAll(/\'/, /\\'/)
						value = value.replaceAll(/\"/, /\\"/)
						// 明示的に文字列を意味する文字列表現にします
						raw = "${cnst.scalar.str}${value}"
						break
					default:
						raw = value
						break
				}
				break
			default:
				throw new TpacHandlingException(String.format(msgs.exc.noSupportScalarString, value, value.class.name))
		}
		return raw
	}
	
	/**
	 * クローンを返します。
	 * @return クローン
	 */
	@Override
	TeaHandle clone(){
		TeaHandle cloned = (TeaHandle) super.clone()
		if (upper != null) cloned.upper = upper.clone()
		cloned.lowers = lowers.clone()
		cloned.comments = comments.clone()
		cloned.map = map.clone()
		return cloned
	}
	
	/**
	 * このハンドルのクローンを指定されたハンドルでマージして返します。<br/>
	 * マージ方法は {@link #mergeRecursive(TeaHandle)}を参照してください。
	 * @param handle マージするハンドル
	 * @return マージしたハンドル
	 */
	TeaHandle plus(TeaHandle handle){
		return clone().mergeRecursive(handle)
	}
	
	/**
	 * このハンドルを指定されたハンドルでマージして返します。<br/>
	 * タグと名前は変わりません。このハンドルのタグと名前をそのまま使います。<br/>
	 * 下位ハンドルは以下の処理をします。</p>
	 * <ul>
	 * <li>タグ名、名前が一致するハンドルは再帰的に下位ハンドルもマージします。</li>
	 * <li>タグ名、名前が一致しないハンドルを追加します。</li>
	 * </ul>
	 * <p>マップは、一致するキーがあれば指定されたハンドルの値で上書き、
	 * なければ新規追加となります。</br>
	 * 本メソッドは破壊的であることに注意してください。</br>
	 * {@link #plus(TeaHandle)}は非破壊的です。
	 * @param handle マージするハンドル
	 * @return マージしたハンドル
	 */
	TeaHandle mergeRecursive(TeaHandle handle){
		handle.lowers.each { String key, TeaHandle lower ->
			if (lowers.containsKey(key)){
				lowers[key].mergeRecursive(lower)
			} else {
				this << lower
			}
		}
		map.putAll(handle.map)
		return this
	}
}

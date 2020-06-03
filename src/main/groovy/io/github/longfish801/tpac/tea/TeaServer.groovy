/*
 * TeaServer.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.tea

import io.github.longfish801.tpac.TpacConst as cnst
import io.github.longfish801.tpac.TpacHandlingException
import io.github.longfish801.tpac.TpacMaker
import io.github.longfish801.tpac.TpacMsg as msgs
import io.github.longfish801.tpac.TpacParty
import io.github.longfish801.tpac.tea.TeaHandle
import io.github.longfish801.tpac.tea.TeaDec
import io.github.longfish801.tpac.tea.TeaParty
import java.util.regex.Matcher

/**
 * tpac文書を保持する特性です。<br/>
 * 複数の tpac文書を保持できます。<br/>
 * スカラー値として指定したハンドルのパス文字列で
 * 他の tpac文書を参照するときには、
 * 参照元および参照先が同じサーバで保持されている
 * 必要があります。<br/>
 * 独自のDSL利用のため、解析器（{@link TeaParty}）を
 * 変更したい場合は {@link #newParty()}を、
 * 宣言のタグに応じて生成器（{@link TeaMaker}）を
 * 変更したい場合は {@link #newMaker(String)}をオーバーライドしてください。
 * @version 0.3.00 2020/05/23
 * @author io.github.longfish801
 */
trait TeaServer {
	/** 識別キーと宣言とのマップ */
	Map<String, TeaDec> decs = [:]
	
	/**
	 * 宣言のタグに対応する生成器を返します。<br/>
	 * 現状、宣言のタグとは無関係に {@link TpacMaker}インスタンスを返します。<br/>
	 * 宣言のタグに応じて生成器を変更したい場合は本メソッドを
	 * オーバーライドしてください。<br/>
	 * ひとつの tpac文書毎に本メソッドで新たなインスタンスを
	 * ひとつ生成してください。<br/>
	 * @param tag 宣言のタグ
	 * @return TeaMaker
	 */
	TeaMaker newMaker(String tag){
		return new TpacMaker()
	}
	
	/**
	 * tpac文書の解析に用いる解析器を返します。<br/>
	 * 現状は {@link TpacParty}インスタンスを返します。<br/>
	 * 独自のDSL利用のため、解析器を変更したい場合は
	 * 本メソッドをオーバーライドしてください。<br/>
	 * ひとつの解析対象毎（{@link #soak(def)}メソッド実行毎）に
	 * 本メソッドで新たなインスタンスをひとつ生成してください。
	 * @return tpac文書の解析器
	 */
	TeaParty newParty(){
		return new TpacParty(server: this)
	}
	
	/**
	 * 対象を解析して tpac文書を生成し保持します。
	 * @param source 解析対象（File、URL、String、BufferedReaderのいずれか）
	 * @return 自インスタンス
	 */
	TeaServer soak(def source){
		newParty().parse(source).each { this << it.validateRecursive() }
		return this
	}
	
	/**
	 * tpac文書の宣言を追加します。
	 * @param dec 宣言
	 * @return 自インスタンス
	 * @exception TpacHandlingException 識別キーが重複する宣言は追加できません
	 */
	TeaServer leftShift(TeaDec dec){
		dec.server = this
		decs[dec.key] = dec
		return this
	}
	
	/**
	 * tpac文書の宣言を参照します。
	 * @param key 識別キー
	 * @return 宣言
	 */
	def getAt(String key){
		return decs.get(key)
	}
	
	/**
	 * tpac文書の宣言を参照します。
	 * @param key 識別キー
	 * @return 宣言
	 */
	TeaDec propertyMissing(String key){
		return getAt(key)
	}
	
	/**
	 * tpac文書からパスに対応するハンドルを参照します。<br/>
	 * パス区切り文字で分割した先頭の識別キーと、残りのパスに分割します。<br/>
	 * 残りのパスがなければ、先頭の識別キーと一致する宣言を返します。<br/>
	 * 残りのパスがあれば、先頭の識別キーと一致する宣言にそれを新たなパスとして依頼します。
	 * @param path パス
	 * @return パスに対応するハンドル（該当するハンドルがなければnull）
	 * @exception TpacHandlingException 統語的にありえないパスです
	 */
	TeaHandle solvePath(String path){
		// パス区切り文字で分割した先頭の要素を解決します
		if (cnst.path.decs.every { !(path ==~ it) }){
			throw new TpacHandlingException(String.format(msgs.exc.invalidpath, path))
		}
		Matcher matcher = Matcher.getLastMatcher()
		String deckey = matcher.group(1)
		String other = (matcher.groupCount() >= 2)? matcher.group(2) : null
		return (other == null)? decs[deckey] : decs[deckey]?.solvePath(other)
	}
	
	/**
	 * 識別キーが正規表現と一致する宣言のリストを取得します。
	 * @param regex 正規表現
	 * @return 識別キーが正規表現と一致する宣言のリスト（みつからない場合は空リスト）
	 */
	List<TeaDec> findAll(String regex){
		return decs.values().findAll { it.key =~ regex }
	}
}

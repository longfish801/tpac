/*
 * TeaServer.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac;

import groovy.util.logging.Slf4j;
import io.github.longfish801.shared.ArgmentChecker;
import io.github.longfish801.tpac.element.TeaHandle;
import io.github.longfish801.tpac.element.TeaDec;
import io.github.longfish801.tpac.element.TpacHandle;
import io.github.longfish801.tpac.parser.TeaParty;
import io.github.longfish801.tpac.parser.TeaMaker;
import io.github.longfish801.tpac.parser.TeaMakerMakeException;
import io.github.longfish801.tpac.parser.TpacMaker;
import java.util.regex.Matcher;

/**
 * tpac文書を保持する特性です。
 * @version 1.0.00 2018/08/16
 * @author io.github.longfish801
 */
@Slf4j('LOG')
trait TeaServer {
	/** 識別キーと宣言とのマップ */
	Map<String, TeaDec> decs = [:];
	/** TeaParty */
	TeaParty teaParty = new TeaParty(this);
	
	/**
	 * 宣言のタグに対応する TeaMakerを返します。
	 * @param tag 宣言のタグ
	 * @return TeaMaker
	 */
	TeaMaker maker(String tag){
		return new TpacMaker();
	}
	
	/**
	 * tpac文書を解析します。
	 * @param reader 処理対象
	 * @return 自インスタンス
	 */
	TeaServer soak(def source){
		try {
			teaParty.soak(source);
		} catch (TeaParty.TeaPartyParseException exc){
			throw new TeaServerParseException("tpac文書の解析が文法誤りのため失敗しました。lineNo=${teaParty.lineNo} line=${teaParty.line}", exc);
		} catch (TeaMakerMakeException exc){
			throw new TeaServerParseException("tpac文書の構築が記述誤りのため失敗しました。lineNo=${teaParty.lineNo} line=${teaParty.line}", exc);
		}
		return this;
	}
	
	/**
	 * tpac文書の宣言を追加します。
	 * @param dec 宣言
	 * @return 自インスタンス
	 */
	TeaServer leftShift(TeaDec dec){
		ArgmentChecker.checkNotNull('宣言', dec);
		ArgmentChecker.checkUniqueKey('下位ハンドラ', dec.key, decs);
		decs[dec.key] = dec;
		return this;
	}
	
	/**
	 * tpac文書の宣言を参照します。
	 * @param key 識別キー
	 * @return 宣言
	 */
	TeaDec getAt(String key){
		return decs[key];
	}
	
	/**
	 * tpac文書からパスに対応するハンドルを参照します。
	 * @param path パス
	 * @return パスに対応するハンドル
	 */
	TeaHandle path(String path){
		ArgmentChecker.checkNotBlank('パス', path);
		if (TpacHandle.cnstTeaHandle.path.decs.every { !(path ==~ it) }){
			throw new IllegalArgumentException("パスから宣言を特定できません。path=${path}");
		}
		Matcher matcher = Matcher.getLastMatcher();
		String dec = matcher.group(1);
		String other = (matcher.groupCount() >= 2)? matcher.group(2) : null;
		TeaHandle hndl = (other == null)? this.getAt(dec) : this.getAt(dec)?.path(other);
		if (hndl == null) throw new IllegalArgumentException("パスから宣言を特定できません。path=${path}");
		return hndl;
	}
}

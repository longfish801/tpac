/*
 * TeaParty.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.tea

import io.github.longfish801.tpac.TpacConst as cnst
import io.github.longfish801.tpac.TpacMsg as msgs
import io.github.longfish801.tpac.TpacParseExceptions
import io.github.longfish801.tpac.TpacSyntaxException
import java.nio.charset.StandardCharsets
import java.util.regex.Matcher
import org.slf4j.LoggerFactory

/**
 * tpac記法を解析するための特性です。<br/>
 * @version 0.3.00 2020/05/23
 * @author io.github.longfish801
 */
trait TeaParty {
	/** ログ出力 */
	private static final def LOG = LoggerFactory.getLogger(TeaParty.class)
	/** サーバ */
	TeaServer server
	/** 生成器のリスト */
	List makers
	/** 解析対象がファイルもしくはURLの場合の想定文字コード */
	String encoding = StandardCharsets.UTF_8.displayName()
	/** 分岐の種類 */
	enum BranchType { OUT, HANDLE, MAPSCALAR, MAPTEXT, TEXT, GAP }
	/** 行の種類 */
	enum LineType { DEC, DECEND, HANDLE, HANDLEEND, COMMENT, MAPSCALAR, MAPTEXT, TEXTDIV, TEXTINVALID, TEXT }
	/** 現在の分岐の種類 */
	private BranchType branchType
	/** 解析行の一行前の種類 */
	private LineType preLineType
	/** 明示的なテキスト範囲の区切り行 */
	private String textDivLine
	/** テキストの行番号 */
	private int textLineNo
	
	/**
	 * ファイル内容を解析し tpac文書のリストを返します。<br/>
	 * 文字コードは UTF-8（メンバ変数encoding）とみなします。
	 * @param file 解析対象のファイル
	 * @return tpac文書の宣言のリスト
	 * @see #parse(BufferedReader)
	 */
	List<TeaDec> parse(File file){
		LOG.debug('parse tpac file. file={}', file.absolutePath)
		return parse(file.newReader(encoding))
	}
	
	/**
	 * URLの参照先を解析し tpac文書のリストを返します。<br/>
	 * 文字コードは UTF-8（メンバ変数encoding）とみなします。
	 * @param url 解析対象のURL
	 * @return tpac文書の宣言のリスト
	 * @see #parse(BufferedReader)
	 */
	List<TeaDec> parse(URL url){
		LOG.debug('parse tpac url. url={}', url)
		return parse(url.newReader(encoding))
	}
	
	/**
	 * 文字列を解析し tpac文書のリストを返します。
	 * @param text 文字列
	 * @return tpac文書の宣言のリスト
	 * @see #parse(BufferedReader)
	 */
	List<TeaDec> parse(String text){
		LOG.debug('parse tpac text')
		return parse(new BufferedReader(new StringReader(text)))
	}
	
	/**
	 * 対象を解析し tpac文書のリストを返します。
	 * @param reader 解析対象のBufferedReader
	 * @return tpac文書の宣言のリスト
	 * @exception TpacParseExceptions tpac文書の解析中に問題が生じました。
	 */
	List<TeaDec> parse(BufferedReader reader){
		branchType = BranchType.OUT
		makers = []
		TpacParseExceptions parseExc = new TpacParseExceptions(msgs.exc.parseError)
		reader.eachLine { String line, int lineNo ->
			try {
				LineType lineType = classifyLine(line)
				LOG.trace('line[{}, {}, {}]={}', lineNo, branchType, lineType, line)
				switch (branchType){
					case BranchType.OUT:
						branchOut(lineType, line)
						break
					case BranchType.HANDLE:
						branchHandle(lineType, line)
						break
					case BranchType.MAPSCALAR:
						branchMapScalar(lineType, line)
						break
					case BranchType.MAPTEXT:
						branchMapText(lineType, line)
						break
					case BranchType.TEXT:
						branchText(lineType, line)
						break
					case BranchType.GAP:
						branchGap(lineType, line)
						break
				}
				preLineType = lineType
			} catch (exc){
				LOG.error("${msgs.exc.parseError} lineNo={}, line={}", lineNo, line, exc)
				parseExc.append(lineNo, line, exc)
			}
		}
		if (!parseExc.errors.empty) throw parseExc
		return makers.collect { it.dec }
	}
	
	/**
	 * 行を分類し、その種類を返します。
	 * @param line 解析対象行
	 * @return 行の種類
	 */
	private LineType classifyLine(String line){
		if (!line.startsWith(cnst.bullet.invalidText)) return LineType.TEXT // テキスト
		switch (line){
			case { it.startsWith(cnst.bullet.dec) }: // 宣言
				return LineType.DEC
			case cnst.bullet.decEnd: // 宣言終端
				return LineType.DECEND
			case { it =~ cnst.bullet.handle || it =~ cnst.bullet.handleAlt }: // ハンドル開始行
				return LineType.HANDLE
			case cnst.bullet.handleEnd: // ハンドル終端
				return LineType.HANDLEEND
			case { it.startsWith(cnst.bullet.comment) }: // コメント
				return LineType.COMMENT
			case { it =~ cnst.line.mapScalar }: // マップ スカラー値指定
				return LineType.MAPSCALAR
			case { it =~ cnst.line.mapText }: // マップ テキスト指定
				return LineType.MAPTEXT
			case { textDivLine == null && it =~ cnst.bullet.textRange }: // 明示的なテキスト範囲の区切り行（開始）
			case { textDivLine != null && it == textDivLine }: // 明示的なテキスト範囲の区切り行（終端）
				return LineType.TEXTDIV
		}
		// 不正なテキスト
		return LineType.TEXTINVALID
	}
	
	/**
	 * 文書の範囲外の行を解析します。
	 * @param lineType 解析対象行の種類
	 * @param line 解析対象行
	 */
	private void branchOut(LineType lineType, String line){
		switch (lineType){
			case LineType.DEC: // 宣言
				branchType = BranchType.HANDLE
				leafDec(line)
				break
		}
	}
	
	/**
	 * 宣言／ハンドル開始行からコメントまでを解析します。
	 * @param lineType 解析対象行の種類
	 * @param line 解析対象行
	 * @exception TpacSyntaxException テキストの先頭に半角シャープは使用できません。
	 */
	private void branchHandle(LineType lineType, String line){
		switch (lineType){
			case LineType.DEC: // 宣言
				leafDec(line)
				break
			case LineType.DECEND: // 宣言終端
				makers.last().createDecEnd()
				branchType = BranchType.OUT
				break
			case LineType.HANDLE: // ハンドル開始行
				leafHandle(line)
				break
			case LineType.HANDLEEND: // ハンドル終端
				makers.last().createHandleEnd()
				branchType = BranchType.GAP
				break
			case LineType.COMMENT: // コメント
				leafComment(line)
				break
			case LineType.MAPSCALAR: // マップ スカラー値指定
				branchType = BranchType.MAPSCALAR
				leafMapScalar(line)
				break
			case LineType.MAPTEXT: // マップ テキスト指定
				branchType = BranchType.MAPTEXT
				leafMapText(line)
				break
			case LineType.TEXTDIV: // 明示的なテキスト範囲の開始
				branchType = BranchType.TEXT
				textDivLine = line
				break
			case LineType.TEXT: // テキスト
				branchType = BranchType.MAPSCALAR
				leafText(line)
				break
			case LineType.TEXTINVALID: // 不正なテキスト
				throw new TpacSyntaxException(msgs.exc.noSharpTop)
		}
	}
	
	/**
	 * 宣言／ハンドルのマップ以降を解析します。
	 * @param lineType 解析対象行の種類
	 * @param line 解析対象行
	 * @exception TpacSyntaxException コメントはマップよりも前に記述してください。
	 * @exception TpacSyntaxException テキストの先頭に半角シャープは使用できません。
	 */
	private void branchMapScalar(LineType lineType, String line){
		switch (lineType){
			case LineType.DEC: // 宣言
				branchType = BranchType.HANDLE
				leafDec(line)
				break
			case LineType.DECEND: // 宣言終端
				makers.last().createDecEnd()
				branchType = BranchType.OUT
				break
			case LineType.HANDLE: // ハンドル開始行
				branchType = BranchType.HANDLE
				leafHandle(line)
				break
			case LineType.HANDLEEND: // ハンドル終端
				makers.last().createHandleEnd()
				branchType = BranchType.GAP
				break
			case LineType.COMMENT: // コメント
				throw new TpacSyntaxException(msgs.exc.commentBeforeMap)
			case LineType.MAPSCALAR: // マップ スカラー値指定
				leafMapScalar(line)
				break
			case LineType.MAPTEXT: // マップ テキスト指定
				branchType = BranchType.MAPTEXT
				leafMapText(line)
				break
			case LineType.TEXTDIV: // 明示的なテキスト範囲の開始
				branchType = BranchType.TEXT
				textDivLine = line
				break
			case LineType.TEXTINVALID: // 不正なテキスト
				throw new TpacSyntaxException(msgs.exc.noSharpTop)
			default: // テキスト
				leafText(line)
		}
	}
	
	/**
	 * マップの値にテキストを指定している箇所を解析します。
	 * @param lineType 解析対象行の種類
	 * @param line 解析対象行
	 * @exception TpacSyntaxException マップの値として空行のテキストは指定できません。
	 */
	private void branchMapText(LineType lineType, String line){
		switch (lineType){
			case LineType.TEXT: // テキスト
				branchType = BranchType.MAPSCALAR
				leafText(line)
				break
			case LineType.TEXTDIV: // 明示的なテキスト範囲の開始
				branchType = BranchType.TEXT
				textDivLine = line
				break
			default: // テキスト以外
				throw new TpacSyntaxException(msgs.exc.cannotBlankMapText)
		}
	}
	
	/**
	 * 明示的なテキスト範囲の行を解析します。
	 * @param lineType 解析対象行の種類
	 * @param line 解析対象行
	 * @exception TpacSyntaxException 空行のテキストは指定できません。
	 */
	private void branchText(LineType lineType, String line){
		switch (lineType){
			case LineType.TEXTDIV: // 明示的なテキスト範囲の終端
				// 前の行がテキスト範囲の開始だった場合
				if (preLineType == LineType.TEXTDIV){
					throw new TpacSyntaxException(msgs.exc.cannotEmptyListText)
				}
				textDivLine = null
				branchType = BranchType.MAPSCALAR
				break
			default: // テキスト
				leafText(line)
		}
	}
	
	/**
	 * ハンドル間の行を解析します。
	 * @param lineType 解析対象行の種類
	 * @param line 解析対象行
	 */
	private void branchGap(LineType lineType, String line){
		switch (lineType){
			case LineType.DEC: // 宣言
				branchType = BranchType.HANDLE
				leafDec(line)
				break
			case LineType.HANDLE: // ハンドル開始行
				branchType = BranchType.HANDLE
				leafHandle(line)
				break
		}
	}
	
	/**
	 * 宣言を解析します。
	 * @param line 解析対象行
	 * @exception TpacSyntaxException 統語的にありえない宣言です。
	 */
	private void leafDec(String line){
		// 宣言から識別キー、スカラー値を解析します
		if (cnst.line.dec.every { !(line ==~ it) }){
			throw new TpacSyntaxException(msgs.exc.invalidDec)
		}
		Matcher matcher = Matcher.lastMatcher
		String key = matcher.group(1)
		String scalar = (matcher.groupCount() >= 2)? matcher.group(2) : null
		// TeaMakerを新規に生成して宣言を作成します
		List splited = splitKey(key)
		makers << server.newMaker(splited[0])
		makers.last().createDec(splited[0], splited[1], scalar)
	}
	
	/**
	 * ハンドル開始行を解析します。
	 * @param line 解析対象行
	 * @exception TpacSyntaxException 統語的にありえないハンドル開始行です。
	 */
	private void leafHandle(String line){
		// ハンドル開始行から階層、識別キー、スカラー値を解析します
		if (cnst.line.handle.every { !(line ==~ it) }){
			throw new TpacSyntaxException(msgs.exc.invalidHandle)
		}
		Matcher matcher = Matcher.lastMatcher
		String levelStr = matcher.group(1)
		String key = matcher.group(2)
		String scalar = (matcher.groupCount() >= 3)? matcher.group(3) : null
		int level = (line =~ cnst.bullet.handle)? Integer.parseInt(levelStr) : levelStr.length()
		// ハンドルを作成します
		List splited = splitKey(key)
		makers.last().createHandle(splited[0], splited[1], level, scalar)
	}
	
	/**
	 * コメントを解析します。
	 * @param line 解析対象行
	 */
	private void leafComment(String line){
		String comment = line.substring(cnst.bullet.comment.length())
		makers.last().createComment(comment)
	}
	
	/**
	 * スカラー値指定のマップを解析します。
	 * @param line 解析対象行
	 */
	private void leafMapScalar(String line){
		line ==~ cnst.line.mapScalar
		Matcher matcher = Matcher.lastMatcher
		String key = matcher.group(1)
		String scalar = matcher.group(2)
		makers.last().createMap(key, scalar)
	}
	
	/**
	 * テキスト指定のマップを解析します。
	 * @param line 解析対象行
	 */
	private void leafMapText(String line){
		line ==~ cnst.line.mapText
		String key = Matcher.lastMatcher.group(1)
		makers.last().createMap(key, null)
	}
	
	/**
	 * テキストを解析します。
	 * @param line 解析対象行
	 */
	private void leafText(String line){
		// 前の行がテキストでなければ行番号をリセットします
		if (preLineType != LineType.TEXT && preLineType != LineType.TEXTINVALID){
			textLineNo = 0
		}
		textLineNo += 1
		makers.last().createText(line, textLineNo)
	}
	
	/**
	 * 識別キーをタグと名前に分離して返します。
	 * @param key
	 * @return 第一要素がタグ、第二要素が名前のリスト
	 * @exception TpacSyntaxException 統語的にありえない識別キーです。
	 */
	private static List splitKey(String key){
		if (cnst.line.key.every { !(key ==~ it) }){
			throw new TpacSyntaxException(msgs.exc.invalidIdKey)
		}
		Matcher matcher = Matcher.lastMatcher
		String tag = matcher.group(1)
		String name = (matcher.groupCount() >= 2)? matcher.group(2) : cnst.dflt.handleName
		return [ tag, name ]
	}
}

/*
 * TeaParty.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.parser;

import groovy.transform.InheritConstructors;
import groovy.util.logging.Slf4j;
import io.github.longfish801.shared.ArgmentChecker;
import io.github.longfish801.shared.ExchangeResource;
import io.github.longfish801.tpac.TeaServer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

/**
 * tpac文書を解析します。<br/>
 * 宣言のタグに応じて{@link TeaMaker}を変更したい場合は、
 * メンバ変数 makerMapにキーとしてタグ名、
 * 値として TeaMakerインスタンスを追加してください。
 * @version 1.0.00 2018/08/16
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TeaParty {
	/** ConfigObject */
	static final ConfigObject cnst = ExchangeResource.config(TeaParty.class);
	/** TeaServer */
	TeaServer server;
	/** TeaMaker */
	TeaMaker maker;
	/** 解析状態 */
	enum ParseStatus { OUT, HANDLE, LIST, MAP, TEXT }
	/** 現在の解析状態 */
	ParseStatus status;
	/** 解析中の行番号 */
	int lineNo;
	/** 解析中の行 */
	String line;
	
	/**
	 * コンストラクタ。
	 * @param server TeaServer
	 */
	TeaParty(TeaServer server){
		this.server = server;
	}
	
	/**
	 * ファイルの内容をtpac文書とみなして解析します。
	 * @param file 処理対象のファイル
	 * @see #soak(BufferedReader)
	 */
	void soak(File file){
		ArgmentChecker.checkNotNull('ファイル', file);
		soak(file.newReader(StandardCharsets.UTF_8.displayName()));
	}
	
	/**
	 * URLの参照先をtpac文書とみなして解析します。
	 * @param url 処理対象のURL
	 * @see #soak(BufferedReader)
	 */
	void soak(URL url){
		ArgmentChecker.checkNotNull('URL', url);
		soak(url.newReader(StandardCharsets.UTF_8.displayName()));
	}
	
	/**
	 * 文字列をtpac文書とみなして解析します。
	 * @param text 文字列
	 * @see #soak(BufferedReader)
	 */
	void soak(String text){
		soak(new BufferedReader(new StringReader(text)));
	}
	
	/**
	 * tpac文書を解析します。
	 * @param reader 処理対象のBufferedReader
	 */
	void soak(BufferedReader reader){
		ArgmentChecker.checkNotNull('処理対象のBufferedReader', reader);
		status = ParseStatus.OUT;
		lineNo = 0;
		line = null;
		reader.eachLine {
			lineNo ++;
			line = it;
			switch (status){
				case ParseStatus.OUT: branchOut(line); break;
				case ParseStatus.HANDLE: branchHandle(line); break;
				default: throw new InternalError("想定外の解析状態です。lineNo=${lineNo} status=${status}");
			}
		}
	}
	
	/**
	 * 文書外もしくはハンドル外の行を解析します。
	 * @param line 解析対象行
	 */
	protected void branchOut(String line){
		switch (line){
			case {it.startsWith(cnst.bol.dec)}: // 宣言
				status = ParseStatus.HANDLE;
				leafDec(line);
				break;
			case {it ==~ cnst.bol.handle}: // ハンドル
				status = ParseStatus.HANDLE;
				leafHandle(line);
				break;
		}
	}
	
	/**
	 * ハンドル内の行を解析します。
	 * @param line 解析対象行
	 */
	protected void branchHandle(String line){
		switch (line){
			case {it.startsWith(cnst.bol.dec)}: // 宣言
				maker.createHandleEnd();
				maker.createDecEnd();
				status = ParseStatus.HANDLE;
				leafDec(line);
				break;
			case cnst.bol.decEnd: // 宣言終端
				maker.createHandleEnd();
				maker.createDecEnd();
				status = ParseStatus.OUT;
				break;
			case {it ==~ cnst.bol.handle}: // ハンドル
				maker.createHandleEnd();
				leafHandle(line);
				status = ParseStatus.HANDLE;
				break;
			case cnst.bol.handleEnd: // ハンドル終端
				maker.createHandleEnd();
				status = ParseStatus.OUT;
				break;
			case {!it.startsWith(cnst.bol.text)}: // テキスト
				leafText(line);
				break;
			case {it ==~ cnst.bol.list}: // リスト
				leafList(line);
				break;
			case {it ==~ cnst.bol.map}: // マップ
				leafMap(line);
				break;
			case {it.startsWith(cnst.bol.comment)}: // コメント
				leafComment(line);
				break;
			default:
				throw new TeaPartyParseException("ハンドル内の記述として文法誤りのため解析できません。");
		}
	}
	
	/**
	 * 宣言を解析します。
	 * @param line 解析対象行
	 */
	protected void leafDec(String line){
		if (cnst.patternLine.decs.every { !(line ==~ it) }){
			throw new TeaPartyParseException("文法誤りのため宣言を解析できません。");
		}
		Matcher matcher = Matcher.getLastMatcher();
		String tag = matcher.group(1);
		String name = (matcher.groupCount() >= 2)? matcher.group(2) : '';
		String scalar = (matcher.groupCount() >= 3)? matcher.group(3) : null;
		maker = server.maker(tag);
		maker.createDec(server, tag, name, scalar);
	}
	
	/**
	 * ハンドル開始行を解析します。
	 * @param line 解析対象行
	 */
	protected void leafHandle(String line){
		if (cnst.patternLine.handles.every { !(line ==~ it) }){
			throw new TeaPartyParseException("文法誤りのためハンドル開始行を解析できません。");
		}
		Matcher matcher = Matcher.getLastMatcher();
		String levelStr = matcher.group(1);
		String tag = matcher.group(2);
		String name = (matcher.groupCount() >= 3)? matcher.group(3) : '';
		String scalar = (matcher.groupCount() >= 4)? matcher.group(4) : null;
		int level;
		switch (levelStr){
			case {it ==~ cnst.bolPattern.handle1}:
				level = Integer.parseInt(Matcher.getLastMatcher().group(1));
				break;
			case {it ==~ cnst.bolPattern.handle2}:
				level = Matcher.getLastMatcher().group(1).length();
				break;
			default:
				throw new TeaPartyParseException("ハンドルの階層の記述の仕方が不正です。levelStr=${levelStr}");
		}
		maker.createHandle(tag, name, level, scalar);
	}
	
	/**
	 * テキストを解析します。
	 * @param line 解析対象行
	 */
	protected void leafText(String line){
		if (line ==~ cnst.bol.textEscaped) line = line.substring(1);
		maker.createText(line);
	}
	
	/**
	 * リストを解析します。
	 * @param line 解析対象行
	 */
	protected void leafList(String line){
		if (cnst.patternLine.list.every { !(line ==~ it) }){
			throw new TeaPartyParseException("文法誤りのためリストを解析できません。");
		}
		Matcher matcher = Matcher.getLastMatcher();
		int level = matcher.group(1).length();
		String scalar = (matcher.groupCount() >= 2)? matcher.group(2) : null;
		maker.createList(scalar, level);
	}
	
	/**
	 * マップを解析します。
	 * @param line 解析対象行
	 */
	protected void leafMap(String line){
		if (cnst.patternLine.map.every { !(line ==~ it) }){
			throw new TeaPartyParseException("文法誤りのためマップを解析できません。");
		}
		Matcher matcher = Matcher.getLastMatcher();
		int level = matcher.group(1).length();
		String key = matcher.group(2);
		String scalar = (matcher.groupCount() >= 3)? matcher.group(3) : null;
		maker.createMap(key, scalar, level);
	}
	
	/**
	 * コメントを解析します。
	 * @param line 解析対象行
	 */
	protected void leafComment(String line){
		if (!(line ==~ cnst.patternLine.comment)){
			throw new TeaPartyParseException("文法誤りのためコメントを解析できません。");
		}
		String comment = Matcher.getLastMatcher().group(1);
		maker.createComment(comment);
	}
	
	/**
	 * tpac文書の解析失敗（行解析）を表す例外クラスです。
	 */
	@InheritConstructors
	class TeaPartyParseException extends Exception { }
}

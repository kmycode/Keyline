/**
 * Asfres
 * jp.asfres.lib.db @ QueryBuilder.java
 * 作成日時：2015/06/13 12:52:14
 */
package jp.asfres.lib.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * データベース上に設定するクエリのビルダです。
 * パラメータを指定して渡すと、それにふさわしいクエリを生成します。
 * また、誤ったパラメータを指定しようとすると、エラーを返します。
 *
 * @author kmy
 * @version 1.1
 * @since 1.0
 */
public class QueryBuilder {

	/** クエリの種類がまだ決まっていない */
	public static final int UNKNOWN_QUERYTYPE = -1;

	/** SELECTクエリ */
	public static final int SELECT = 1;

	/** UPDATEクエリ */
	public static final int UPDATE = 2;

	/** INSERTクエリ */
	public static final int INSERT = 3;

	/** CREATEクエリ */
	public static final int CREATE = 4;

	/** ALTERクエリ */
	public static final int ALTER = 5;

	/** CREATE IFクエリ */
	public static final int CREATEIF = 6;

	/** DELETEクエリ */
	public static final int DELETE = 7;

	/** クエリの種類 */
	private int queryType;

	/** SELECTなどで抽出するカラム */
	private ArrayList<String> columnList;

	/** FROM句 */
	private ArrayList<String> fromList;

	/** WHERE句 */
	private ArrayList<String> whereList;

	/** ORDERBY句 */
	private ArrayList<String> orderbyList;

	/** ORDERBYで、DESCかASCか */
	private boolean isAsc;

	/** JOIN句 */
	private ArrayList<Join> joinList;

	/** LIMITで取得する行数 */
	private int limitNum = -1;

	/** LIMITで取得する最初の行番号 */
	private int limitStartNum = 0;

	/** テーブルオブジェクト */
	private Table table;

	/** 最後に参照したテーブル */
	private static Table lastTable;

	/** デバッグモード */
	private static boolean debugMode = false;

	/** 最後に出力したクエリ。デバッグ用 */
	private static String lastQuery = "";

	/** クエリビルダによって現在存在するインスタンス数（デバッグ用） */
	private static int instanceCounter = 0;

	/**
	 * ファイナライザ（TODO:メモリリークが発生しているか確認するためのデバッグ用。リリース時には消す）
	 *
	 * @version 1.1
	 * @since 1.1
	 */
	@Override
	protected void finalize() {
		try {
			super.finalize();
		}
		catch (Throwable e) {
		}
		finally {
			QueryBuilder.instanceCounter--;
			System.out.println("QueryBuilder: " + QueryBuilder.instanceCounter);
		}
	}

	/**
	 * コンストラクタです。
	 * クエリのビルドをスタートします。
	 *
	 * @param intructionType
	 *            クエリの種類です。SELECT、UPDATEなどの定数を指定します。
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder(int intructionType) {
		this.queryType = intructionType;
		this.columnList = new ArrayList<String>();
		this.fromList = new ArrayList<String>();
		this.whereList = new ArrayList<String>();
		this.orderbyList = new ArrayList<String>();
		this.joinList = new ArrayList<Join>();
		this.isAsc = false;

		// TODO: リリース時に消す
		QueryBuilder.instanceCounter++;
		System.out.println("QueryBuilder: " + QueryBuilder.instanceCounter);
	}

	/**
	 * クエリの種類を指定しないデフォルトコンストラクタです。
	 * このコンストラクタを指定すると、select()、update()などのメソッドによって
	 * クエリの種類を指定する必要が生じます。
	 * クエリのビルドをスタートします。
	 *
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder() {
		this(QueryBuilder.UNKNOWN_QUERYTYPE);
	}

	/**
	 * テーブルの名前を設定します。
	 * このメソッドを使用すると、テーブルオブジェクトが新規に作成されます。
	 * CREATEなどで用います。
	 *
	 * @param tn
	 *            テーブル名
	 * @return テーブルオブジェクト
	 * @version 1.0
	 * @since 1.0
	 */
	public Table tableName(String tn) {
		this.table = new Table(tn);
		return this.getTable();
	}

	/**
	 * 名前を持たないテーブルを新規作成します。
	 * このメソッドを利用して作成したテーブルは、最後に呼ばれたテーブルとして記録されません。
	 * なので、ここで作成したテーブルを後から取得することはできません。
	 * ALTERなどで用います。
	 *
	 * @return テーブルオブジェクト
	 * @version 1.0
	 * @since 1.0
	 */
	public Table emptyTable() {
		this.table = new Table("");
		return this.table.pushQueryBuilder(this);
	}

	/**
	 * テーブルオブジェクトを設定します。
	 * CREATEなどで用いられます。
	 * スレッドセーフを確保するため、
	 * 内部ではcloneのテーブルオブジェクトが保存されます。
	 *
	 * @param t
	 *            テーブルオブジェクト
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @throws SQLException
	 *             クローン生成に失敗した場合、SQLに失敗したとみなしてこの例外がスローされます。
	 * @since 1.0
	 */
	public QueryBuilder setTable(Table t) throws SQLException {
		try {
			this.table = t.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new SQLException("Making table clone failed!");
		}
		return this;
	}

	/**
	 * このビルダが保持しているテーブルオブジェクトを取得します。
	 * これにより、テーブルを設定できるようになります。
	 * なお、テーブルからクエリビルダに戻るためには、Table.popQueryBuilder()を使用します。<br>
	 * ここで参照したテーブルは保存され、static getLastTable()によって参照することができます。
	 *
	 * @see Table#popQueryBuilder()
	 * @return テーブルオブジェクト
	 * @version 1.0
	 * @since 1.0
	 */
	public Table getTable() {
		QueryBuilder.lastTable = this.table;
		return this.table.pushQueryBuilder(this);
	}

	/**
	 * このビルダが保持しているテーブルオブジェクトを取得します。
	 * これにより、テーブルを設定できるようになります。
	 * なお、テーブルからクエリビルダに戻るためには、Table.popQueryBuilder()を使用します。<br>
	 * ここで参照したテーブルは保存され、static getLastTable()によって参照することができます。
	 *
	 * @see Table#popQueryBuilder()
	 * @return テーブルオブジェクト
	 * @version 1.0
	 * @since 1.0
	 */
	public Table push() {
		return this.getTable();
	}

	/**
	 * このビルダからgetTableを使って最後に参照したテーブルを返します。
	 *
	 * @return 最後に参照したテーブル
	 * @version 1.0
	 * @since 1.0
	 */
	public static Table getLastTable() {
		return QueryBuilder.lastTable.reset();
	}

	/**
	 * 以前に生成されたビルダのインスタンスが最後に参照したテーブルを、もう一度このビルダのテーブルとして設定します。
	 * そして、テーブルオブジェクトを呼び出します。
	 *
	 * @return テーブルオブジェクト
	 * @version 1.0
	 * @since 1.0
	 */
	public Table lastTable() {
		this.table = QueryBuilder.lastTable;
		return this.table.pushQueryBuilder(this).reset();
	}

	/**
	 * クエリの種類を返します。
	 * このクエリビルダで設定されたものがどのような命令であるかを確認できます。
	 * 定数SELECT、INSERTなどの値が返されます。
	 *
	 * @return クエリの種類を表す定数
	 * @version 1.0
	 * @since 1.0
	 */
	public int getQueryType() {
		return this.queryType;
	}

	/**
	 * クエリの種類をSELECTにします。
	 *
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder select() {
		this.queryType = QueryBuilder.SELECT;
		return this;
	}

	/**
	 * クエリの種類をINSERTにします。
	 *
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder insert() {
		this.queryType = QueryBuilder.INSERT;
		return this;
	}

	/**
	 * クエリの種類をUPDATEにします。
	 *
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder update() {
		this.queryType = QueryBuilder.UPDATE;
		return this;
	}

	/**
	 * クエリの種類をCREATEにします。
	 *
	 * @return 1.0
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder create() {
		this.queryType = QueryBuilder.CREATE;
		return this;
	}

	/**
	 * クエリの種類をALTERにします。
	 *
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder alter() {
		this.queryType = QueryBuilder.ALTER;
		return this;
	}

	/**
	 * クエリの種類をCREATE IF NOT EXISTS...にします。
	 *
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder createif() {
		this.queryType = QueryBuilder.CREATEIF;
		return this;
	}

	/**
	 * クエリの種類をDELETEにします。
	 *
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder delete() {
		this.queryType = QueryBuilder.DELETE;
		return this;
	}

	/**
	 * デバッグモードを設定します。trueでデバッグモードが有効になります。
	 *
	 * @param mode
	 *            新しいデバッグモードの状態
	 * @version 1.1
	 * @since 1.1
	 */
	public static void setDebugMode(boolean mode) {
		QueryBuilder.debugMode = mode;
	}

	/**
	 * クエリビルダで指定されたデータを、文字列に出力します。
	 * ここで返される文字列を、直接データベースにクエリとして発行することができます。 {@link #setDebugMode(boolean)}
	 * によってデバッグモードが有効である時は、クエリ文字列がコンソール画面に表示され、
	 * 戻り値には空の文字列がセットされます。
	 *
	 * @see java.lang.Object#toString()
	 * @version 1.1
	 * @since 1.0
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean isFirst = false;

		switch (this.queryType) {

		case SELECT: {
			sb.append("select ");
			isFirst = true;
			for (String str : this.columnList) {
				if (!isFirst) {
					sb.append(",");
				}
				sb.append(str);
				isFirst = false;
			}
			sb.append(" from ");
			isFirst = true;
			for (String str : this.fromList) {
				if (!isFirst) {
					sb.append(",");
				}
				sb.append("`").append(str).append("`");
				isFirst = false;
			}
			if (this.joinList.size() > 0) {
				sb.append(" ");
				for (Join j : this.joinList) {
					sb.append(j.toString()).append(" ");
				}
			}
			if (this.whereList.size() > 0) {
				sb.append(" where ");
				isFirst = true;
				for (String str : this.whereList) {
					if (!isFirst) {
						sb.append(" and ");
					}
					sb.append("(").append(str).append(")");
					isFirst = false;
				}
			}
			if (this.orderbyList.size() > 0) {
				sb.append(" order by ");
				isFirst = true;
				for (String str : this.orderbyList) {
					if (!isFirst) {
						sb.append(",");
					}
					sb.append("`").append(str).append("`");
					isFirst = false;
				}
				sb.append(this.isAsc ? " asc" : " desc");
			}
			if (this.limitNum > 0) {
				sb.append(" limit ").append(this.limitStartNum).append(",").append(this.limitNum);
			}
			break;
		}

		case CREATE: {
			sb.append("create table `").append(this.table.getTableName()).append("` (")
					.append(this.table.queryCreate()).append(")");
			break;
		}

		case CREATEIF: {
			sb.append("create table if not exists `").append(this.table.getTableName()).append("` (")
					.append(this.table.queryCreate()).append(")");
			break;
		}

		case INSERT: {
			sb.append("insert `").append(this.table.getTableName()).append("` values(")
			.append(this.table.queryInsert()).append(")");
			break;
		}

		case UPDATE: {
			sb.append("update `").append(this.table.getTableName()).append("` set ").append(this.table.queryUpdate());
			if (this.whereList.size() > 0) {
				sb.append(" where ");
				isFirst = true;
				for (String str : this.whereList) {
					if (!isFirst) {
						sb.append(" and ");
					}
					sb.append("(").append(str).append(")");
					isFirst = false;
				}
			}
			break;
		}

		case ALTER: {
			sb.append("alter ").append(this.table.queryAlter());
			break;
		}

		case DELETE: {
			sb.append("delete `");
			sb.append(this.fromList.get(0));
			sb.append("` from ");
			isFirst = true;
			for (String str : this.fromList) {
				if (!isFirst) {
					sb.append(",");
				}
				sb.append("`").append(str).append("`");
				isFirst = false;
			}
			if (this.joinList.size() > 0) {
				sb.append(" ");
				for (Join j : this.joinList) {
					sb.append(j.toString()).append(" ");
				}
			}
			if (this.whereList.size() > 0) {
				sb.append(" where ");
				isFirst = true;
				for (String str : this.whereList) {
					if (!isFirst) {
						sb.append(" and ");
					}
					sb.append("(").append(str).append(")");
					isFirst = false;
				}
			}
			break;
		}
		}

		// クエリ出力
		// QueryBuilder.lastQuery = sb.append(";").toString();
		QueryBuilder.lastQuery = sb.toString();
		if (!QueryBuilder.debugMode) {
			return QueryBuilder.lastQuery;
		}
		else {
			System.out.println(sb.toString());
			return "";
		}
	}

	/**
	 * クエリビルダが最後に {@link #toString()} で発行したクエリを返します。
	 * このメソッドは、デバッグを意図して作られたものです。
	 *
	 * @return 最後に発行したクエリ
	 * @version 1.1
	 * @since 1.1
	 */
	public static String getLastQuery() {
		return QueryBuilder.lastQuery;
	}

	/**
	 * データベースオブジェクトを渡して、クエリを実行します。
	 *
	 * @param db
	 *            データベースオブジェクト
	 * @return クエリの実行結果
	 * @throws SQLException
	 *             データベースとのやりとりに失敗した場合、この例外がスローされます。
	 * @version 1.1
	 * @since 1.1
	 */
	public ResultSet exe(MyDB db) throws SQLException {
		if (this.queryType == QueryBuilder.SELECT) {
			return db.query(this.toString());
		}
		else {
			db.queryNoResult(this.toString());
			return null;
		}
	}

	/**
	 * カラムを指定します。SELECTなどで結果に含めるよう指定します。
	 *
	 * @param cl
	 *            カラム名
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder column(String cl) {
		this.columnList.add("`" + cl + "`");
		return this;
	}

	/**
	 * SELECTにおいて、COUNT関数を利用します。
	 *
	 * @param cl
	 *            カラム名
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder count(String cl) {
		this.columnList.add("count(" + cl + ")");
		return this;
	}

	/**
	 * SELECTにおいて、MAX関数を利用します。
	 *
	 * @param cl
	 *            カラム名
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder max(String cl) {
		this.columnList.add("max(" + cl + ")");
		return this;
	}

	/**
	 * SELECTにおいて、MIN関数を利用します。
	 *
	 * @param cl
	 *            カラム名
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder min(String cl) {
		this.columnList.add("min(" + cl + ")");
		return this;
	}

	/**
	 * SELECTにおいて、所定の関数を利用します。
	 *
	 * @param funcName
	 *            関数名
	 * @param funcVal
	 *            関数に渡す値
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder func(String funcName, String funcVal) {
		this.columnList.add(funcName + "(" + funcVal + ")");
		return this;
	}

	/**
	 * SELECTにおいて、全てのカラムを選択します。
	 *
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder allColumn() {
		this.columnList.add("*");
		return this;
	}

	/**
	 * クエリの実行に利用するテーブルを指定します。
	 *
	 * @param tn
	 *            テーブル名
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder from(String tn) {
		this.fromList.add(tn);
		return this;
	}

	/**
	 * クエリの実行に利用するテーブルを指定します。
	 *
	 * @param t
	 *            テーブルのオブジェクト
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder from(Table t) {
		return this.from(t.getTableName());
	}

	/**
	 * 条件式を指定します。
	 * ここで追加された条件式は、全てANDとして連結されます。
	 *
	 * @param ws
	 *            条件式
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder where(String ws) {
		this.whereList.add(ws);
		return this;
	}

	/**
	 * 式の両辺と記号を指定することにより、条件式を設定します。
	 * ここで追加された条件式は、全てANDとして連結されます。
	 * なお、カラム同士の比較には、{@link #whereColumn(String, String, String)}を使ってください。
	 *
	 * @param set1
	 *            条件式の左辺。カラム名であることを想定
	 * @param sig
	 *            条件式の比較器号
	 * @param set2
	 *            条件式の右辺。sigがlikeであれば文字列、そうでなければカラム名であることを想定
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder where(String set1, String sig, String set2) {
		if (!sig.equals("like")) {
			return this.where("`" + set1 + "` " + sig + " `" + set2 + "`");
		}
		return this.where("`" + set1 + "` " + sig + " '" + set2 + "'");
	}

	/**
	 * 式の両辺と記号を指定することにより、条件式を設定します。
	 * ここで追加された条件式は、全てANDとして連結されます。
	 *
	 * @param set1
	 *            条件式の左辺。カラム名であることを想定
	 * @param sig
	 *            条件式の比較器号
	 * @param set2
	 *            条件式の右辺。数値であることを想定
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder where(String set1, String sig, int set2) {
		return this.where("`" + set1 + "` " + sig + " " + set2);
	}

	/**
	 * 式の両辺と記号を指定することにより、条件式を設定します。
	 * ここで追加された条件式は、全てANDとして連結されます。
	 *
	 * @param set1
	 *            条件式の左辺。カラム名であることを想定
	 * @param sig
	 *            条件式の比較器号
	 * @param set2
	 *            条件式の右辺。浮動小数点数であることを想定
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder where(String set1, String sig, double set2) {
		return this.where("`" + set1 + "` " + sig + " " + set2);
	}

	/**
	 * 式の両辺と記号を指定することにより、条件式を設定します。
	 * ここで追加された条件式は、全てANDとして連結されます。
	 *
	 * @param set1
	 *            条件式の左辺。カラム名であることを想定
	 * @param sig
	 *            条件式の比較器号
	 * @param set2
	 *            条件式の右辺。64bit整数であることを想定
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder where(String set1, String sig, long set2) {
		return this.where("`" + set1 + "` " + sig + " " + set2);
	}

	/**
	 * サブクエリの実行結果を条件式の右辺に設定します。
	 * ここで追加された条件式は、全てANDとして連結されます。
	 *
	 * @param set1
	 *            条件式の左辺。カラム名であることを想定
	 * @param sig
	 *            条件式の比較器号
	 * @param set2
	 *            条件式の右辺。サブクエリであることを想定
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder where(String set1, String sig, QueryBuilder set2) {
		return this.where(set1, sig, "(" + set2.toString() + ")");
	}

	/**
	 * JOIN句を追加します。
	 *
	 * @param tn
	 *            追加するテーブル名
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder join(String tn) {
		Join j = new Join(Join.INNER);
		j.setTable(tn);
		this.joinList.add(j);
		return this;
	}

	/**
	 * JOIN句を追加します。
	 *
	 * @param tn
	 *            追加するテーブル
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder join(Table t) {
		return this.join(t.getTableName());
	}

	/**
	 * LEFT JOIN句を追加します。
	 *
	 * @param tn
	 *            追加するテーブル名
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder leftjoin(String tn) {
		Join j = new Join(Join.LEFT);
		j.setTable(tn);
		this.joinList.add(j);
		return this;
	}

	/**
	 * LEFT JOIN句を追加します。
	 *
	 * @param qb
	 *            サブクエリをあらわすクエリビルダオブジェクト
	 * @param asName
	 *            サブクエリをあらわすエイリアス名
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder leftjoin(QueryBuilder qb, String asName) {
		Join j = new Join(Join.LEFT);
		j.setTable(qb, asName);
		this.joinList.add(j);
		return this;
	}

	/**
	 * LEFT JOIN句を追加します。
	 *
	 * @param tn
	 *            追加するテーブル
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder leftjoin(Table t) {
		return this.leftjoin(t.getTableName());
	}

	/**
	 * RIGHT JOIN句を追加します。
	 *
	 * @param tn
	 *            追加するテーブル名
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder rightjoin(String tn) {
		Join j = new Join(Join.RIGHT);
		j.setTable(tn);
		this.joinList.add(j);
		return this;
	}

	/**
	 * RIGHT JOIN句を追加します。
	 *
	 * @param tn
	 *            追加するテーブル
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder rightjoin(Table t) {
		return this.rightjoin(t.getTableName());
	}

	/**
	 * JOINにおける条件式を指定します。
	 * ここで追加された条件式は、全てANDとして連結されます。
	 *
	 * @param ws
	 *            条件式
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder on(String ws) {
		int size = this.joinList.size();
		if (size > 0) {
			this.joinList.get(size - 1).on(ws);
		}
		return this;
	}

	/**
	 * JOINにおいて、式の両辺と記号を指定することにより、条件式を設定します。
	 * ここで追加された条件式は、全てANDとして連結されます。
	 *
	 * @param set1
	 *            条件式の左辺。カラム名であることを想定
	 * @param sig
	 *            条件式の比較器号
	 * @param set2
	 *            条件式の右辺。sigがlikeであれば文字列、そうでなければカラム名であることを想定
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder on(String set1, String sig, String set2) {
		if (!sig.equals("like")) {
			return this.on("`" + set1 + "` " + sig + " `" + set2 + "`");
		}
		return this.on("`" + set1 + "` " + sig + " '" + set2 + "'");
	}

	/**
	 * JOINにおいて、式の両辺と記号を指定することにより、条件式を設定します。
	 * ここで追加された条件式は、全てANDとして連結されます。
	 *
	 * @param set1
	 *            条件式の左辺。カラム名であることを想定
	 * @param sig
	 *            条件式の比較器号
	 * @param set2
	 *            条件式の右辺。数値であることを想定
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder on(String set1, String sig, int set2) {
		return this.on("`" + set1 + "` " + sig + " " + set2);
	}

	/**
	 * JOINにおいて、式の両辺と記号を指定することにより、条件式を設定します。
	 * ここで追加された条件式は、全てANDとして連結されます。
	 *
	 * @param set1
	 *            条件式の左辺。カラム名であることを想定
	 * @param sig
	 *            条件式の比較器号
	 * @param set2
	 *            条件式の右辺。浮動小数点数であることを想定
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder on(String set1, String sig, double set2) {
		return this.on("`" + set1 + "` " + sig + " " + set2);
	}

	/**
	 * JOINにおいて、式の両辺と記号を指定することにより、条件式を設定します。
	 * ここで追加された条件式は、全てANDとして連結されます。
	 *
	 * @param set1
	 *            条件式の左辺。カラム名であることを想定
	 * @param sig
	 *            条件式の比較器号
	 * @param set2
	 *            条件式の右辺。64bit整数であることを想定
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder on(String set1, String sig, long set2) {
		return this.on("`" + set1 + "` " + sig + " " + set2);
	}

	/**
	 * 指定したカラムがNULLであるかを判定します。
	 * この条件式は、WHEREに追加されます。
	 *
	 * @param columnName
	 *            判定するカラム名
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder isnull(String columnName) {
		return this.where("`" + columnName + "` is NULL");
	}

	/**
	 * 指定したカラムがNULLでないかを判定します。
	 * この条件式は、WHEREに追加されます。
	 *
	 * @param columnName
	 *            判定するカラム名
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder isnotnull(String columnName) {
		return this.where("`" + columnName + "` is not NULL");
	}

	/**
	 * 指定したカラムが、指定した文字列と合致しているかを判定します。
	 * この条件式は、WHEREに追加されます。
	 *
	 * @param columnName
	 *            判定するカラム名
	 * @param str
	 *            判定に利用する文字列
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder like(String columnName, String str) {
		return this.where("`" + columnName + "` like '" + str + "'");
	}

	/**
	 * 指定したカラムが、指定したカラムと合致しているかを判定します。
	 * この条件式は、WHEREに追加されます。
	 *
	 * @param columnName
	 *            判定するカラム名
	 * @param sig
	 *            比較演算子
	 * @param column2Name
	 *            判定するカラム名
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder whereColumn(String columnName, String sig, String column2Name) {
		return this.where("`" + columnName + "` " + sig + " `" + column2Name + "`");
	}

	/**
	 * 並べ替えるカラムを指定します。
	 * 並べ替えの向きは、desc()、asc()メソッドで指定します。
	 *
	 * @param os
	 *            並べ替えるカラム
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder orderby(String os) {
		this.orderbyList.add(os);
		return this;
	}

	/**
	 * ORDER BYで並べ替える時の向きをDESCに設定します。
	 *
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder desc() {
		this.isAsc = false;
		return this;
	}

	/**
	 * ORDER BYで並べ替える時の向きをASCに設定します。
	 *
	 * @return このオブジェクト自身
	 * @version 1.0
	 * @since 1.0
	 */
	public QueryBuilder asc() {
		this.isAsc = true;
		return this;
	}

	/**
	 * LIMITで表示する行数を設定します。0未満の数値を指定すると、LIMITが解除されます。
	 *
	 * @param num
	 *            表示する行数
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder limit(int num) {
		this.limitNum = num;
		return this;
	}

	/**
	 * LIMITで抽出する行について、最初の行番号を指定します。
	 *
	 * @param num
	 *            表示する行数
	 * @param start
	 *            最初に表示する行番号。0から始まる
	 * @return このオブジェクト自身
	 * @version 1.1
	 * @since 1.1
	 */
	public QueryBuilder limit(int num, int start) {
		this.limitNum = num;
		this.limitStartNum = num;
		return this;
	}

	/**
	 * ひとつのテーブルをあらわします。
	 * このクラスのオブジェクトは、INSERTやCREATEなどでテーブルの型チェックに用いられます。
	 *
	 * @author kmy
	 * @version 1.1
	 * @since 1.0
	 */
	public static class Table implements Cloneable {

		// （100以上は、データをセットするときに '' がいらないタイプ。整数など）

		/** 文字列型 */
		public static final int CHARACTERS = 1;

		/** 文字列型 */
		public static final int TEXT = 2;

		/** 日付型 */
		public static final int DATE = 3;

		/** 日時型 */
		public static final int DATETIME = 4;

		/** 時刻型 */
		public static final int TIME = 5;

		/** 文字列型 */
		public static final int TINYTEXT = 6;

		/** 文字列型 */
		public static final int MEDIUMTEXT = 7;

		/** 文字列型 */
		public static final int LONGTEXT = 8;

		/** 数値型 */
		public static final int INT8 = 101;

		/** 数値型 */
		public static final int INT16 = 102;

		/** 数値型 */
		public static final int INT24 = 103;

		/** 数値型 */
		public static final int INT32 = 104;

		/** 数値型 */
		public static final int INT64 = 105;

		/** 論理型 */
		public static final int BOOLEAN = 106;

		/** 単精度浮動小数点数 */
		public static final int FLOAT = 107;

		/** 倍精度浮動小数点数 */
		public static final int DOUBLE = 108;

		/** カラム名 */
		private ArrayList<String> columnNameList;

		/** カラムの型 */
		private ArrayList<Integer> columnTypeList;

		/** カラムの桁数 */
		private ArrayList<Integer> columnSizeList;

		/** カラムに代入する要素の内容 */
		private ArrayList<String> columnSetList;

		/** カラムのNOT NULLフラグ */
		private ArrayList<Boolean> columnNotNullList;

		/** カラムの主キーフラグ */
		private ArrayList<Boolean> columnPrimaryKeyList;

		/** カラムのUNSIGNEDフラグ */
		private ArrayList<Boolean> columnUnsignedList;

		/** AUTO_INCREMENTを設定するカラムの番号（配列のインデックス） */
		private int columnAiIndex;

		/** テーブル名 */
		private String tableName;

		/** ALTERで対象とするテーブル名 */
		private Table alterTable;

		/** ALTERで挿入する直前のカラム。最初や最後に挿入するときは定数 */
		private int alterAddColumn;

		/** ALTERで最初に挿入する時 */
		private static final int ALTER_FIRST = -1;

		/** ALTERで最後に挿入する時 */
		private static final int ALTER_LAST = -2;

		/** ALTERの処理のタイプ（追加 or 削除） */
		private int alterType;

		/** ALTERでカラムを追加する時 */
		private static final int ALTER_ADD = 1;

		/** ALTERでカラムを削除する時 */
		private static final int ALTER_DEL = 2;

		/** 対応するクエリビルダオブジェクト */
		private QueryBuilder queryBuilder;

		/** カラム追加をロックするか */
		private boolean isLockColumn;

		/** 新規追加するカラムに、常にNOT NULLを設定するか */
		private boolean isNotNull;

		/** 生成されたインスタンス数のカウンタ。デバッグ用 */
		private static int instanceCounter = 0;

		/**
		 * TODO:ファイナライザです。デバッグ用、リリース時には消す
		 *
		 * @see java.lang.Object#finalize()
		 * @version 1.1
		 * @since 1.1
		 */
		@Override
		protected void finalize() {
			try {
				super.finalize();
			}
			catch (Throwable e) {
			}
			finally {
				Table.instanceCounter--;
				System.out.println("QueryBuilder.Table: " + Table.instanceCounter);
			}
		}

		/**
		 * コンストラクタです。
		 * 内部データの初期化などを行います。
		 *
		 * @param tn
		 *            テーブル名
		 * @version 1.0
		 * @since 1.0
		 */
		public Table(String tn) {
			this.columnNameList = new ArrayList<String>();
			this.columnTypeList = new ArrayList<Integer>();
			this.columnSizeList = new ArrayList<Integer>();
			this.columnSetList = new ArrayList<String>();
			this.columnNotNullList = new ArrayList<Boolean>();
			this.columnPrimaryKeyList = new ArrayList<Boolean>();
			this.columnUnsignedList = new ArrayList<Boolean>();
			this.columnAiIndex = -1;
			this.tableName = tn;
			this.isLockColumn = false;
			this.isNotNull = false;
			this.alterAddColumn = Table.ALTER_LAST;
			Table.instanceCounter++;
			System.out.println("QueryBuilder.Table: " + Table.instanceCounter);
		}

		/**
		 * コンストラクタです。
		 * 内部データの初期化などを行います。
		 *
		 * @param tn
		 *            テーブル名
		 * @param qbobj
		 *            対応するクエリビルダ
		 * @version 1.0
		 * @since 1.0
		 */
		public Table(String tn, QueryBuilder qbobj) {
			this(tn);
			this.queryBuilder = qbobj;
		}

		/**
		 * テーブルオブジェクトが再び呼び出されたさい、しかるべきデータをリセットし、このオブジェクトを再利用できる状態にします。
		 * なお、カラム名や型など、テーブル固有の情報は保持されます。
		 *
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table reset() {
			int size = this.columnNameList.size();
			for (int i = 0; i < size; i++) {
				this.columnSetList.set(i, null);
			}

			// ALTERでカラム追加位置
			this.alterAddColumn = Table.ALTER_LAST;

			// カラムの追加をロックする
			this.isLockColumn = true;

			return this;
		}

		/**
		 * このオブジェクトがあらわす文字列を返します。
		 * クエリビルダが設定されている場合は、そのtoString()を呼び出します。
		 * これは、テーブルオブジェクトが設定された状態でこのメソッドが呼び出された時の対策です。
		 *
		 * @see java.lang.Object#toString()
		 * @version 1.0
		 * @since 1.0
		 */
		@Override
		public String toString() {

			// カラムの追加をロックする
			this.isLockColumn = true;

			if (this.queryBuilder != null) {
				return this.popQueryBuilder().toString();
			}
			return super.toString();
		}

		/**
		 * クエリを実行します。
		 * すでに設定されているクエリビルダをpopし、{@link QueryBuilder#exe(MyDB)}を実行します。
		 * クエリビルダが設定されていなければ、例外NullPointerExceptionを返します。
		 *
		 * @param db
		 *            データベースオブジェクト
		 * @return クエリ実行結果
		 * @throws SQLException
		 *             データベースとのやりとりに失敗した場合、この例外がスローされます。
		 * @version 1.1
		 * @since 1.1
		 */
		public ResultSet exe(MyDB db) throws SQLException {
			return this.pop().exe(db);
		}

		/**
		 * このメソッドは何もしません。 {@link QueryBuilder#push()}との互換性をもつために用意されたものです。
		 *
		 * @return このオブジェクト自身
		 * @version 1.1
		 * @since 1.1
		 */
		public Table push() {
			return this;
		}

		/**
		 * クエリビルダをプッシュし、自身を返します。
		 * これは、クエリビルダからテーブルを設定したいときに用いられます。
		 *
		 * @param qbobj
		 *            オブジェクトに設定するクエリビルダ
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table pushQueryBuilder(QueryBuilder qbobj) {
			this.queryBuilder = qbobj;
			return this;
		}

		/**
		 * スタックしているクエリビルダを返します。
		 * クエリビルダを返すと、設定されたクエリビルダはリセットされます。
		 * これは、ガベージコレクションで相互参照によるメモリリークを防止するための仕様です。
		 *
		 * @return このオブジェクトに設定されているクエリビルダ
		 * @version 1.0
		 * @since 1.0
		 */
		public QueryBuilder popQueryBuilder() {
			QueryBuilder qb = this.queryBuilder;
			this.queryBuilder = null;
			return qb;
		}

		/**
		 * スタックしているクエリビルダを返します。
		 * クエリビルダを返すと、設定されたクエリビルダはリセットされます。
		 * これは、ガベージコレクションで相互参照によるメモリリークを防止するための仕様です。
		 *
		 * @return このオブジェクトに設定されているクエリビルダ
		 * @version 1.0
		 * @since 1.0
		 */
		public QueryBuilder pop() {
			return this.popQueryBuilder();
		}

		/**
		 * このオブジェクトに設定されたテーブル名を取得します。
		 *
		 * @return テーブル名
		 * @version 1.0
		 * @since 1.0
		 */
		public String getTableName() {
			return this.tableName;
		}

		/**
		 * テーブルオブジェクトのクローンを作成します。
		 *
		 * @see java.lang.Object#clone()
		 * @version 1.0
		 * @since 1.0
		 */
		@Override
		public Table clone() throws CloneNotSupportedException {
			Table c = (Table) super.clone();
			c.columnSetList = new ArrayList<String>();
			for (int i = 0; i < c.columnNameList.size(); i++) {
				c.columnSetList.add("");
			}
			Table.instanceCounter++;
			System.out.println("QueryBuilder.Table: " + Table.instanceCounter);
			return c.reset();
		}

		/**
		 * CREATEクエリで利用する形式で文字列を返します。
		 *
		 * @return 生成された文字列
		 * @version 1.0
		 * @since 1.0
		 */
		public String queryCreate() {
			StringBuilder sb = new StringBuilder();
			int size = this.columnNameList.size();
			for (int i = 0; i < size; i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append("`").append(this.columnNameList.get(i)).append("` ")
						.append(this.getTypeName(this.columnTypeList.get(i)));
				if (this.columnSizeList.get(i) > 0) {
					sb.append("(").append(this.columnSizeList.get(i)).append(")");
				}
				if (this.columnUnsignedList.get(i)) {
					sb.append(" unsigned");
				}
				if (this.columnNotNullList.get(i)) {
					sb.append(" not null");
				}
				if (this.columnPrimaryKeyList.get(i)) {
					sb.append(" primary key");
				}
				if (this.columnAiIndex == i) {
					sb.append(" auto_increment");
				}
			}
			return sb.toString();
		}

		/**
		 * INSERTクエリで利用する形式で文字列を返します。
		 * NOT NULLが指定されているカラムには空のデータを、
		 * そうでないカラムにはNULLを指定します。
		 * また、NOT NULLが設定されていても、値が設定されていない限り
		 * AUTO_INCREMENTが設定されているカラムには、NULLを出力します。
		 *
		 * @return 生成された文字列
		 * @version 1.1
		 * @since 1.0
		 */
		public String queryInsert() {
			StringBuilder sb = new StringBuilder();
			int size = this.columnNameList.size();
			for (int i = 0; i < size; i++) {
				if (i > 0) {
					sb.append(",");
				}
				if (this.columnSetList.get(i) != null) {
					if (this.columnTypeList.get(i) >= 100) {
						sb.append(this.columnSetList.get(i));
					}
					else {
						sb.append("'").append(this.columnSetList.get(i)).append("'");
					}
				}
				else {
					if (!this.columnNotNullList.get(i) || i == this.columnAiIndex) {
						sb.append("NULL");
					}
					else if (this.columnTypeList.get(i) >= 100) {
						sb.append(0);
					}
					else {
						sb.append("''");
					}
				}
			}
			return sb.toString();
		}

		/**
		 * UPDATEクエリで利用する形式で文字列を返します。
		 *
		 * @return 生成された文字列
		 * @version 1.0
		 * @since 1.0
		 */
		public String queryUpdate() {
			StringBuilder sb = new StringBuilder();
			int size = this.columnNameList.size();
			boolean isFirst = true;
			for (int i = 0; i < size; i++) {
				if (this.columnSetList.get(i) == null) {
					continue;
				}
				if (!isFirst) {
					sb.append(",");
				}
				isFirst = false;
				if (this.columnTypeList.get(i) >= 100) {
					sb.append("`").append(this.columnNameList.get(i)).append("`=").append(this.columnSetList.get(i));
				}
				else {
					sb.append("`").append(this.columnNameList.get(i)).append("`='").append(this.columnSetList.get(i))
					.append("'");
				}
			}
			return sb.toString();
		}

		/**
		 * ALTERクエリで利用する形式で文字列を返します。
		 *
		 * @return 生成された文字列
		 * @version 1.0
		 * @since 1.0
		 */
		public String queryAlter() {
			StringBuilder sb = new StringBuilder();
			sb.append("`").append(this.alterTable.getTableName()).append("` ");
			if (this.alterType == Table.ALTER_ADD) {
				sb.append("add ").append(this.queryCreate());
			}
			else if (this.alterType == Table.ALTER_DEL) {
				sb.append("del ");
				boolean firstFlag = true;
				for (String str : this.columnNameList) {
					if (!firstFlag) {
						sb.append(",");
					}
					firstFlag = false;
					sb.append("`").append(str).append("`");
				}
			}

			if (this.alterAddColumn == Table.ALTER_FIRST) {
				sb.append(" first");
			}
			else if (this.alterAddColumn != Table.ALTER_LAST) {
				sb.append(" after `").append(this.alterTable.columnNameList.get(this.alterAddColumn)).append("`");
			}
			return sb.toString();
		}

		/**
		 * 指定したカラム名に文字列データをセットします。
		 *
		 * @param columnName
		 *            カラム名
		 * @param into
		 *            セットする内容
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table set(String columnName, String into) {
			int size = this.columnNameList.size();
			columnfor: for (int i = 0; i < size; i++) {
				if (this.columnNameList.get(i).equals(columnName)) {
					this.columnSetList.set(i, into);
					break columnfor;
				}
			}
			return this;
		}

		/**
		 * 指定したカラム名に日付データをセットします。
		 * カラムの種類によって、日時、日付、時刻のどれを出力するのかを
		 * このメソッドが自動で判別します。
		 * その代わり、カラムの種類が日時に関係する型である必要があります。
		 * 文字列型などに値を代入したいときは、このメソッドではなく自力で日時を文字列に変換する必要があります。
		 *
		 * @param columnName
		 *            カラム名
		 * @param calendar
		 *            セットするカレンダーオブジェクト
		 * @return このオブジェクト自身
		 * @version 1.1
		 * @since 1.1
		 */
		public Table set(String columnName, Calendar calendar) {
			int size = this.columnNameList.size();
			columnfor: for (int i = 0; i < size; i++) {
				if (this.columnNameList.get(i).equals(columnName)) {
					switch (this.columnTypeList.get(i)) {
					case DATETIME:
						this.columnSetList.set(i, MyDB.datetimeToString(calendar));
						break;
					case DATE:
						this.columnSetList.set(i, MyDB.dateToString(calendar));
						break;
					case TIME:
						this.columnSetList.set(i, MyDB.timeToString(calendar));
						break;
					}
					break columnfor;
				}
			}
			return this;
		}

		/**
		 * 指定したカラム名に整数データをセットします。
		 *
		 * @param columnName
		 *            カラム名
		 * @param into
		 *            セットする内容
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table set(String columnName, int into) {
			return this.set(columnName, String.valueOf(into));
		}

		/**
		 * 指定したカラム名に64bit整数データをセットします。
		 *
		 * @param columnName
		 *            カラム名
		 * @param into
		 *            セットする内容
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table set(String columnName, long into) {
			return this.set(columnName, String.valueOf(into));
		}

		/**
		 * 指定したカラム名にフラグデータをセットします。
		 *
		 * @param columnName
		 *            カラム名
		 * @param into
		 *            セットする内容
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table set(String columnName, boolean into) {
			return this.set(columnName, into ? "TRUE" : "FALSE");
		}

		/**
		 * 指定したカラム名に実数データをセットします。
		 *
		 * @param columnName
		 *            カラム名
		 * @param into
		 *            セットする内容
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table set(String columnName, double into) {
			return this.set(columnName, String.valueOf(into));
		}

		/**
		 * 指定したカラム名に実数データをセットします。
		 *
		 * @param columnName
		 *            カラム名
		 * @param into
		 *            セットする内容
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table set(String columnName, float into) {
			return this.set(columnName, String.valueOf(into));
		}

		/**
		 * データベースにおける、データ型の名前を返します。
		 *
		 * @param type
		 *            型の種類
		 * @return 型の名前
		 * @version 1.1
		 * @since 1.0
		 */
		public String getTypeName(int type) {
			if (type == Table.CHARACTERS) {
				return "varchar";
			}
			if (type == Table.TEXT) {
				return "text unicode";
			}
			if (type == Table.TINYTEXT) {
				return "tinytext unicode";
			}
			if (type == Table.MEDIUMTEXT) {
				return "mediumtext unicode";
			}
			if (type == Table.LONGTEXT) {
				return "longtext unicode";
			}
			if (type == Table.INT32) {
				return "int";
			}
			if (type == Table.INT8) {
				return "tinyint";
			}
			if (type == Table.INT16) {
				return "smallint";
			}
			if (type == Table.INT24) {
				return "mediumint";
			}
			if (type == Table.INT64) {
				return "bigint";
			}
			if (type == Table.BOOLEAN) {
				return "boolean";
			}
			if (type == Table.FLOAT) {
				return "float";
			}
			if (type == Table.DOUBLE) {
				return "double";
			}
			if (type == Table.DATE) {
				return "date";
			}
			if (type == Table.DATETIME) {
				return "datetime";
			}
			if (type == Table.TIME) {
				return "time";
			}
			return "";
		}

		/**
		 * さっき追加したカラムに、全てfalseの状態のカラムフラグをセットします。
		 * このメソッドは、カラムを作るたび必ず呼び出さなければいけません。
		 *
		 * @version 1.0
		 * @since 1.0
		 */
		private void setColumnFlag() {
			this.columnNotNullList.add(this.isNotNull);
			this.columnPrimaryKeyList.add(false);
			this.columnSetList.add(null);
			this.columnUnsignedList.add(false);
		}

		/**
		 * さっき追加したカラムに、NOT NULL属性を付加します。
		 *
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table notnull() {
			int size = this.columnNotNullList.size();
			if (size > 0) {
				this.columnNotNullList.set(size - 1, true);
			}
			return this;
		}

		/**
		 * さっき追加したカラムに、NULLを許可します。
		 *
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table oknull() {
			int size = this.columnNotNullList.size();
			if (size > 0) {
				this.columnNotNullList.set(size - 1, false);
			}
			return this;
		}

		/**
		 * これから追加するカラムすべてに自動的にNOT NULLを付加するかを設定します。
		 *
		 * @param b
		 *            設定値
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table notnull(boolean b) {
			this.isNotNull = b;
			return this;
		}

		/**
		 * さっき追加したカラムを主キーに設定します。
		 *
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table primarykey() {
			int size = this.columnPrimaryKeyList.size();
			if (size > 0) {
				this.columnPrimaryKeyList.set(size - 1, true);
			}
			return this;
		}

		/**
		 * さっき追加したカラムに、UNSIGNED属性を付加します。
		 *
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table unsigned() {
			int size = this.columnUnsignedList.size();
			if (size > 0) {
				int type = this.columnTypeList.get(size - 1);
				if (type == Table.INT32 || type == Table.INT8 || type == Table.INT16 || type == Table.INT64) {
					this.columnUnsignedList.set(size - 1, true);
				}
			}
			return this;
		}

		/**
		 * さっき追加したカラムに、AUTO_INCREMENT属性を追加します。
		 * AUTO_INCREMENT属性は１テーブルで１カラムしか設定できないため、
		 * 常に最後に設定されたカラムにのみ、この設定が適用されることになります。
		 *
		 * @return このオブジェクト自身
		 * @version 1.1
		 * @since 1.1
		 */
		public Table ai() {
			this.columnAiIndex = this.columnNameList.size() - 1;
			return this;
		}

		/**
		 * テーブルに新たにカラムを設定します。
		 *
		 * @param name
		 *            カラム名
		 * @param type
		 *            カラムの型
		 * @param size
		 *            サイズ
		 * @return 自身のオブジェクト
		 * @version 1.0
		 * @since 1.0
		 */
		private Table appendType(String name, int type, int size) {
			if (this.isLockColumn) {
				return this;
			}
			this.columnNameList.add(name);
			this.columnTypeList.add(type);
			this.columnSizeList.add(size);
			this.setColumnFlag();
			return this;
		}

		/**
		 * テーブルに文字列型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @param size
		 *            サイズ
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table character(String name, int size) {
			return this.appendType(name, Table.CHARACTERS, size);
		}

		/**
		 * テーブルに文字列型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @param size
		 *            サイズ（バイト数）
		 * @return このオブジェクト自身
		 * @version 1.1
		 * @since 1.0
		 */
		public Table text(String name, int size) {
			return this.appendType(name, Table.TEXT, 0);
		}

		/**
		 * テーブルに整数型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @param size
		 *            サイズ
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table int32(String name, int size) {
			return this.appendType(name, Table.INT32, size);
		}

		/**
		 * テーブルに整数型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table int32(String name) {
			return this.appendType(name, Table.INT32, -1);
		}

		/**
		 * テーブルに16bit整数型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @param size
		 *            サイズ
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table int16(String name, int size) {
			return this.appendType(name, Table.INT16, size);
		}

		/**
		 * テーブルに16bit整数型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table int16(String name) {
			return this.appendType(name, Table.INT16, -1);
		}

		/**
		 * テーブルに8bit整数型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @param size
		 *            サイズ
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table int8(String name, int size) {
			return this.appendType(name, Table.INT8, size);
		}

		/**
		 * テーブルに8bit整数型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table int8(String name) {
			return this.appendType(name, Table.INT8, -1);
		}

		/**
		 * テーブルに24bit整数型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @param size
		 *            サイズ
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table int24(String name, int size) {
			return this.appendType(name, Table.INT24, size);
		}

		/**
		 * テーブルに24bit整数型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table int24(String name) {
			return this.appendType(name, Table.INT24, -1);
		}

		/**
		 * テーブルに単精度小数型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table floats(String name) {
			return this.appendType(name, Table.FLOAT, -1);
		}

		/**
		 * テーブルに倍精度小数型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table doubles(String name) {
			return this.appendType(name, Table.DOUBLE, -1);
		}

		/**
		 * テーブルに日付型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table date(String name) {
			return this.appendType(name, Table.DATE, -1);
		}

		/**
		 * テーブルに日時型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table datetime(String name) {
			return this.appendType(name, Table.DATETIME, -1);
		}

		/**
		 * テーブルに時刻型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @return このオブジェクト自身
		 * @version 1.1
		 * @since 1.1
		 */
		public Table time(String name) {
			return this.appendType(name, Table.TIME, -1);
		}

		/**
		 * テーブルに論理型を追加します。
		 *
		 * @param name
		 *            カラム名
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table bool(String name) {
			return this.appendType(name, Table.BOOLEAN, -1);
		}

		/**
		 * ALTERで、対象となるテーブルを指定します。
		 * 指定されたテーブルでは、queryAlterの実行時にカラム情報が自動で追加されます。
		 *
		 * @param alter
		 *            対象となるテーブル
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table setAlterTable(Table alter) {
			this.alterTable = alter;
			return this;
		}

		/**
		 * ALTERで、カラム追加位置を先頭に指定します。
		 *
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table first() {
			this.alterAddColumn = Table.ALTER_FIRST;
			return this;
		}

		/**
		 * ALTERで、カラム追加位置を最後に指定します。
		 *
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table last() {
			this.alterAddColumn = Table.ALTER_LAST;
			return this;
		}

		/**
		 * ALTERで、カラム追加位置を任意のカラムの直後に指定します。
		 * 該当するカラム名が見つからなかった場合、挿入位置はカラムの最後にセットされます。
		 *
		 * @param columnName
		 *            追加位置の直前のカラム名
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table after(String columnName) {
			int size = this.alterTable.columnNameList.size();
			this.alterAddColumn = Table.ALTER_LAST;
			for (int i = 0; i < size; i++) {
				if (this.alterTable.columnNameList.get(i).equals(columnName)) {
					this.alterAddColumn = i;
					break;
				}
			}
			return this;
		}

		/**
		 * ALTERで、モードを指定します。指定カラムの後に挿入するADDモードを指定します。
		 *
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table add() {
			this.alterType = Table.ALTER_ADD;
			return this;
		}

		/**
		 * ALTERで、モードを指定します。指定カラムを削除するDELモードを指定します。
		 *
		 * @return このオブジェクト自身
		 * @version 1.0
		 * @since 1.0
		 */
		public Table del() {
			this.alterType = Table.ALTER_DEL;
			return this;
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 * @version
	 * @since
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.columnList == null) ? 0 : this.columnList.hashCode());
		result = prime * result + ((this.fromList == null) ? 0 : this.fromList.hashCode());
		result = prime * result + (this.isAsc ? 1231 : 1237);
		result = prime * result + ((this.orderbyList == null) ? 0 : this.orderbyList.hashCode());
		result = prime * result + this.queryType;
		result = prime * result + ((this.table == null) ? 0 : this.table.hashCode());
		result = prime * result + ((this.whereList == null) ? 0 : this.whereList.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @version
	 * @since
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		QueryBuilder other = (QueryBuilder) obj;
		if (this.columnList == null) {
			if (other.columnList != null) {
				return false;
			}
		}
		else if (!this.columnList.equals(other.columnList)) {
			return false;
		}
		if (this.fromList == null) {
			if (other.fromList != null) {
				return false;
			}
		}
		else if (!this.fromList.equals(other.fromList)) {
			return false;
		}
		if (this.isAsc != other.isAsc) {
			return false;
		}
		if (this.orderbyList == null) {
			if (other.orderbyList != null) {
				return false;
			}
		}
		else if (!this.orderbyList.equals(other.orderbyList)) {
			return false;
		}
		if (this.queryType != other.queryType) {
			return false;
		}
		if (this.table == null) {
			if (other.table != null) {
				return false;
			}
		}
		else if (!this.table.equals(other.table)) {
			return false;
		}
		if (this.whereList == null) {
			if (other.whereList != null) {
				return false;
			}
		}
		else if (!this.whereList.equals(other.whereList)) {
			return false;
		}
		return true;
	}

	/**
	 * クエリのJOIN句を取り扱う、プライベートなインナークラスです。
	 *
	 * @author kmy
	 * @version 1.1
	 * @since 1.1
	 */
	private static class Join {

		/** JOINするテーブル名 */
		private String tableName;

		/** tableNameがサブクエリであるかのフラグ */
		private boolean isSubQuery = false;

		/** JOINのモード */
		private int joinMode = -1;

		/** INNER JOIN */
		private static final int INNER = 1;

		/** LEFT JOIN */
		private static final int LEFT = 2;

		/** RIGHT JOIN */
		private static final int RIGHT = 3;

		/** ONの条件 */
		private ArrayList<String> onList;

		/**
		 * JOIN句を扱うインスタンスにおけるコンストラクタです。
		 * 各フィールドの初期化を行います。
		 *
		 * @param mode
		 *            JOINのモード。INNER、LEFT、RIGHTから選択
		 * @version 1.1
		 * @since 1.1
		 */
		public Join(int mode) {
			this.onList = new ArrayList<String>();
			this.joinMode = mode;
		}

		/**
		 * JOINするテーブル名をセットします。
		 *
		 * @param tn
		 *            テーブル名
		 * @version 1.1
		 * @since 1.1
		 */
		public void setTable(String tn) {
			this.tableName = tn;
			this.isSubQuery = false;
		}

		/**
		 * JOINする対象をサブクエリとしてセットします。
		 *
		 * @param q
		 *            サブクエリを表すクエリビルダオブジェクト
		 * @param asName
		 *            サブクエリで構成されるテーブルを表す別名
		 * @version 1.1
		 * @since 1.1
		 */
		public void setTable(QueryBuilder q, String asName) {
			this.tableName = "(" + q.toString() + ") " + asName;
			this.isSubQuery = true;
		}

		/**
		 * ONの条件を追加します。
		 * これら追加された条件は、全てANDで連結されます。
		 *
		 * @param str
		 *            追加する条件式
		 * @version 1.1
		 * @since 1.1
		 */
		public void on(String str) {
			this.onList.add(str);
		}

		/**
		 * 設定されたJOIN句に対応する文字列を出力します。
		 * この式は、クエリの一部として利用することができます。
		 *
		 * @see java.lang.Object#toString()
		 * @version 1.1
		 * @since 1.1
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.joinMode == Join.LEFT ? "left" : this.joinMode == Join.RIGHT ? "right" : "")
			.append(" join ").append(this.isSubQuery ? "" : "`").append(this.tableName)
			.append(this.isSubQuery ? "" : "`").append(" on ");
			boolean isFirst = true;
			for (String str : this.onList) {
				if (!isFirst) {
					sb.append(" and ");
				}
				isFirst = false;
				sb.append("(").append(str).append(")");
			}
			return sb.toString();
		}
	}

}

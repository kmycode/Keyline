package jp.asfres.lib.db;
/**
 * Keyline
 * jp.asfres.lib.db @ SQLite.java
 * 作成日時：2015/06/02 21:33:36
 */


import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQLiteを取り扱うクラスです。
 *
 * @author kmy
 * @version 0.1
 * @since 0.1
 */
public class SQLite extends MyDB {

	/**
	 * データベースに接続します。
	 * SQLiteではファイル参照となります。このため、このconnectメソッドは、クラスの動的ロード以外は何もしません。
	 *
	 * @see jp.asfres.lib.db.MyDB#connect()
	 * @exception SQLException
	 *                クラスパスのロードに失敗した時も、このエラーがスローされます。
	 * @version 0.1
	 * @since 0.1
	 */
	@Override
	public void connect() throws SQLException {
		MyDB.isClassLoad = true;
		this.connection = null;
		this.statement = null;

		// クラスの動的ロード
		try {
			Class.forName("org.sqlite.JDBC");
		}
		catch (ClassNotFoundException e) {
			throw new SQLException();
		}
	}

	/**
	 * データベースを選択する時、接続情報をあらわす文字列を返します。
	 * この文字列は、SQLite固有のものです。
	 *
	 * @see jp.asfres.lib.db.MyDB#getDBName(java.lang.String)
	 * @param dbname
	 *            データベース名
	 * @return データベースへの接続情報をあらわす文字列
	 * @version 0.1
	 * @since 0.1
	 */
	@Override
	protected String getDBName(String dbname) {
		return "jdbc:sqlite:" + dbname;
	}

	/**
	 * 指定した名前のテーブルが存在するか確認します。
	 *
	 * @see jp.asfres.lib.db.MyDB#isTableExist(String)
	 * @param tableName
	 *            存在を確認するテーブル名
	 * @return テーブルが存在すればtrue、存在しなければfalse。データ取得に失敗した場合にもfalse
	 * @version 0.1
	 * @since 0.1
	 */
	@Override
	public boolean isTableExist(String tableName) throws SQLException {

		ResultSet rs = null;
		boolean result;

		rs = this
				.query("select count(*) from sqlite_master where type like 'table' and name like '"
						+ tableName + "';");
		result = rs.getBoolean(1);

		return result;
	}

	/**
	 * 指定したサイズにあったSQLite特有の整数型名を返します。
	 * このメソッドは、create table文などで用いられます。<br>
	 * サイズは、2進数の桁数で指定します。
	 * また、その型のカラムが取りうる最大値を考慮する必要があります。
	 *
	 * @param size
	 *            型に必要なサイズを2進の桁数で指定
	 * @return サイズに見合った型の名前
	 * @version 0.1
	 * @since 0.1
	 */
	@Override
	public final String intType(int size) {
		return "integer";
	}

	/**
	 * 指定したサイズにあったSQLite特有の文字列型名を返します。
	 * このメソッドは、create table文などで用いられます。<br>
	 * サイズは、文字列のバイト数で指定します。
	 * また、その型のカラムが取りうる最大文字列長を考慮する必要があります。
	 *
	 * @param size
	 *            型に必要なサイズをバイト数で指定
	 * @return サイズに見合った型の名前
	 * @version 0.1
	 * @since 0.1
	 */
	@Override
	public final String stringType(int size) {
		return "text";
	}

	/**
	 * 日付、時刻両方の情報をとるSQLite特有の日時型名を返します。
	 * このメソッドは、create table文などで用いられます。
	 *
	 * @return 型の名前
	 * @version 0.1
	 * @since 0.1
	 */
	@Override
	public final String datetimeType() {
		return "text";
	}
}

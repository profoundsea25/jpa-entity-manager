package persistence.sql.ddl;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import persistence.sql.Dialect;
import persistence.sql.entity.EntityColumn;
import persistence.sql.entity.EntityData;

import java.util.List;

import static persistence.sql.Dialect.CREATE_STATEMENT;

/**
 * CREATE 쿼리 생성
 */
public class CreateQueryBuilder {

    private final Dialect dialect;

    public CreateQueryBuilder(Dialect dialect) {
        this.dialect = dialect;
    }

    public String generateQuery(EntityData entityData) {
        return String.format(CREATE_STATEMENT,
                entityData.getTableName(),
                columnPart(entityData) + primaryKeyPart(entityData.getPrimaryKey())
        );
    }

    private String columnPart(EntityData entityData) {
        StringBuilder stringBuilder = new StringBuilder();
        List<EntityColumn> entityColumns = entityData.getEntityColumns().getEntityColumnList();
        for (EntityColumn entityColumn : entityColumns) {
            stringBuilder.append(getColumnPartInCreateQuery(entityColumn));
        }
        return stringBuilder.toString();
    }

    /**
     * Create 문의 컬럼 부분을 생성
     */
    private String getColumnPartInCreateQuery(EntityColumn entityColumn) {
        StringBuilder columnQueryPart = new StringBuilder();

        // 컬럼명
        appendColumnName(entityColumn, columnQueryPart);

        // 컬럼타입
        appendColumnType(entityColumn, columnQueryPart);

        // id인 경우 생성 방법 명시
        appendIdAttributesIfId(entityColumn, columnQueryPart);

        // not null (필요시)
        appendNotNullIfNeed(entityColumn, columnQueryPart);

        // 끝 처리 (comma & space)
        appendForNext(columnQueryPart);

        return columnQueryPart.toString();
    }

    private void appendColumnName(EntityColumn entityColumn, StringBuilder columnQueryPart) {
        columnQueryPart.append(entityColumn.getColumnName());
        columnQueryPart.append(" ");
    }

    private void appendColumnType(EntityColumn entityColumn, StringBuilder columnQueryPart) {
        Class<?> entityColumnType = entityColumn.getType();
        columnQueryPart.append(dialect.getDbType(entityColumnType));
        if (entityColumnType == String.class) {
            columnQueryPart.append(dialect.getStringLength(entityColumn.getField()));
        }
    }

    private void appendIdAttributesIfId(EntityColumn entityColumn, StringBuilder columnQueryPart) {
        if (entityColumn.isId() && entityColumn.getField().isAnnotationPresent(GeneratedValue.class)) {
            columnQueryPart.append(" generated by default as ");
            columnQueryPart.append(entityColumn.getField().getAnnotation(GeneratedValue.class).strategy().name().toLowerCase());
        }
    }

    private void appendNotNullIfNeed(EntityColumn entityColumn, StringBuilder columnQueryPart) {
        if (doesNeedNotNull(entityColumn)) {
            columnQueryPart.append(" not null");
        }
    }

    private boolean doesNeedNotNull(EntityColumn entityColumn) {
        return checkId(entityColumn) || checkColumn(entityColumn);
    }

    private boolean checkId(EntityColumn entityColumn) {
        return entityColumn.isId() && !entityColumn.getField().isAnnotationPresent(GeneratedValue.class);
    }

    private boolean checkColumn(EntityColumn entityColumn) {
        return entityColumn.getField().isAnnotationPresent(Column.class) && !entityColumn.getField().getAnnotation(Column.class).nullable();
    }

    private void appendForNext(StringBuilder columnQueryPart) {
        columnQueryPart.append(", ");
    }

    /**
     * Create 문의 Primary Key 부분 생성
     */
    private String primaryKeyPart(EntityColumn primaryKey) {
        return "primary key (" + primaryKey.getColumnName() + ")";
    }

}

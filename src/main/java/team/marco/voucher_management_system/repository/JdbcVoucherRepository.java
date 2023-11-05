package team.marco.voucher_management_system.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import team.marco.voucher_management_system.model.LoadedVoucher;
import team.marco.voucher_management_system.model.Voucher;
import team.marco.voucher_management_system.type_enum.VoucherType;
import team.marco.voucher_management_system.util.UUIDConverter;

@Profile("prod")
@Repository
public class JdbcVoucherRepository implements VoucherRepository {
    private static final RowMapper<Voucher> voucherRowMapper = JdbcVoucherRepository::mapToVoucher;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcVoucherRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static Voucher mapToVoucher(ResultSet resultSet, int ignored) throws SQLException {
        byte[] idBytes = resultSet.getBytes("id");
        String typeString = resultSet.getString("type");
        String dataString = resultSet.getString("data");

        UUID id = UUIDConverter.convert(idBytes);
        VoucherType type = VoucherType.valueOf(typeString);
        int data = Integer.parseInt(dataString);
        LocalDateTime createAt = resultSet.getTimestamp("created_at").toLocalDateTime();

        LoadedVoucher loadedVoucher = new LoadedVoucher(id, type, data, createAt);

        return VoucherType.convertVoucher(loadedVoucher);
    }

    @Override
    public void save(Voucher voucher) {
        int updateCount = jdbcTemplate.update(
                "INSERT INTO voucher(id, type, data) VALUES (UUID_TO_BIN(:id), :type, :data)",
                voucherToMap(voucher));

        if (updateCount != 1) {
            throw new DataAccessResourceFailureException("Insert query not committed");
        }
    }

    @Override
    public List<Voucher> findAll() {
        return jdbcTemplate.query("SELECT * FROM voucher", voucherRowMapper);
    }

    @Override
    public Optional<Voucher> findById(UUID id) {
        try {
            Voucher voucher = jdbcTemplate.queryForObject(
                    "SELECT * FROM voucher"
                            + " WHERE id = UUID_TO_BIN(:id)",
                    Collections.singletonMap("id", id.toString().getBytes()),
                    voucherRowMapper);

            return Optional.ofNullable(voucher);
        } catch (EmptyResultDataAccessException | UncategorizedSQLException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Voucher> findByType(VoucherType voucherType) {
        return jdbcTemplate.query(
                "SELECT * FROM voucher"
                        + " WHERE type = :type",
                Collections.singletonMap("type", voucherType.toString()),
                voucherRowMapper);
    }

    private Map<String, Object> voucherToMap(Voucher voucher) {
        return Map.ofEntries(
                Map.entry("id", voucher.getId().toString().getBytes()),
                Map.entry("type", voucher.getType().toString()),
                Map.entry("data", voucher.getData()));
    }
}

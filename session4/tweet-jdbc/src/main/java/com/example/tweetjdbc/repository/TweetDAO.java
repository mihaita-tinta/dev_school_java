package com.example.tweetjdbc.repository;

import com.example.tweetjdbc.model.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class TweetDAO {
    private static final Logger LOG = LoggerFactory.getLogger(TweetDAO.class);

    private static class TweetMapper implements RowMapper<Tweet> {

        @Nullable
        @Override
        public Tweet mapRow(ResultSet resultSet, int i) throws SQLException {
            return new Tweet(resultSet.getLong("id"),
                             resultSet.getLong("owner_id"),
                             resultSet.getTimestamp("created").toLocalDateTime(),
                             resultSet.getString("text"));
        }
    }

    private JdbcTemplate jdbcTemplate;

    public TweetDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Tweet> getTweets() {
        return jdbcTemplate.query("select * from tweet", new TweetDAO.TweetMapper());
    }

    public List<Tweet> getTweetsByOwner(String owner) {
        try {
            return jdbcTemplate.query("select * from tweet join user u on u.id = tweet.owner_id where owner_data.name = ?", new Object[]{owner},
                    new TweetDAO.TweetMapper());
        }
        catch (DataAccessException ex){
            return new ArrayList<Tweet>();
        }
    }

    public Optional<Tweet> getTweet(Long id) {
        try {
            return Optional.of(
                    jdbcTemplate.queryForObject("select * from tweet where id = ?", new Object[]{id},
                    new TweetDAO.TweetMapper()));
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    public Tweet insertTweet(Tweet tweet) {
        Map<String, Object> params = new HashMap<>();
        params.put("owner_id", tweet.getOwner_id());
        params.put("datetime", LocalDateTime.now());
        params.put("text", tweet.getText());

        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate).withTableName("tweet")
                .usingGeneratedKeyColumns("id");
        Number id = insert.executeAndReturnKey(params);
        return getTweet((long)id).get();
    }

    public void deleteTweet(Long id) {
        jdbcTemplate.update("DELETE from tweet where id = ?", new Object[] {id});
    }
}
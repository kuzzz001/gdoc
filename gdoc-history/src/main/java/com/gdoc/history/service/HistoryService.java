package com.gdoc.history.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdoc.history.entity.GdocOperationLog;
import com.gdoc.history.entity.GdocSnapshot;
import com.gdoc.history.mapper.OperationLogMapper;
import com.gdoc.history.mapper.SnapshotMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HistoryService {

    private static final Logger log = LoggerFactory.getLogger(HistoryService.class);
    private static final int SNAPSHOT_INTERVAL = 50;

    private final OperationLogMapper operationLogMapper;
    private final SnapshotMapper snapshotMapper;

    public HistoryService(OperationLogMapper operationLogMapper, SnapshotMapper snapshotMapper) {
        this.operationLogMapper = operationLogMapper;
        this.snapshotMapper = snapshotMapper;
    }

    public void logOperation(Long docId, Long userId, String username, int opType, String operations, int version, String contentSnapshot) {
        GdocOperationLog logEntry = new GdocOperationLog();
        logEntry.setDocId(docId);
        logEntry.setUserId(userId);
        logEntry.setUsername(username);
        logEntry.setOperationType(opType);
        logEntry.setOperations(operations);
        logEntry.setVersion(version);
        logEntry.setContentSnapshot(contentSnapshot);
        logEntry.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(logEntry);
    }

    @Transactional
    public void maybeCreateSnapshot(Long docId, int currentVersion, String content, String delta, Long userId) {
        if (currentVersion % SNAPSHOT_INTERVAL == 0) {
            GdocSnapshot snapshot = new GdocSnapshot();
            snapshot.setDocId(docId);
            snapshot.setVersion(currentVersion);
            snapshot.setContent(content);
            snapshot.setDelta(delta);
            snapshot.setOperationCount(SNAPSHOT_INTERVAL);
            snapshot.setCreatedBy(userId);
            snapshot.setCreatedAt(LocalDateTime.now());
            snapshotMapper.insert(snapshot);
            log.info("Created snapshot for doc {} at version {}", docId, currentVersion);
        }
    }

    public List<GdocSnapshot> getSnapshots(Long docId, int page, int size) {
        Page<GdocSnapshot> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<GdocSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GdocSnapshot::getDocId, docId)
                .orderByDesc(GdocSnapshot::getVersion);
        Page<GdocSnapshot> result = snapshotMapper.selectPage(pageObj, wrapper);
        return result.getRecords();
    }

    public GdocSnapshot getSnapshot(Long docId, int version) {
        LambdaQueryWrapper<GdocSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GdocSnapshot::getDocId, docId)
                .eq(GdocSnapshot::getVersion, version);
        return snapshotMapper.selectOne(wrapper);
    }

    public List<GdocOperationLog> getOperationLogs(Long docId, int fromVersion, int toVersion) {
        LambdaQueryWrapper<GdocOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GdocOperationLog::getDocId, docId)
                .between(GdocOperationLog::getVersion, fromVersion, toVersion)
                .orderByAsc(GdocOperationLog::getVersion);
        return operationLogMapper.selectList(wrapper);
    }

    public List<GdocOperationLog> getRecentOperations(Long docId, int limit) {
        LambdaQueryWrapper<GdocOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GdocOperationLog::getDocId, docId)
                .orderByDesc(GdocOperationLog::getId)
                .last("LIMIT " + limit);
        return operationLogMapper.selectList(wrapper);
    }

    @Transactional
    public void rollbackToVersion(Long docId, int targetVersion) {
        GdocSnapshot snapshot = getSnapshot(docId, targetVersion);
        if (snapshot == null) {
            log.warn("No snapshot found for doc {} at version {}", docId, targetVersion);
            return;
        }

        LambdaQueryWrapper<GdocOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GdocOperationLog::getDocId, docId)
                .gt(GdocOperationLog::getVersion, targetVersion);
        operationLogMapper.delete(wrapper);
        log.info("Rolled back doc {} to version {}", docId, targetVersion);
    }

    public String getContentAtVersion(Long docId, int version) {
        GdocSnapshot nearestSnapshot = findNearestSnapshot(docId, version);
        if (nearestSnapshot == null) {
            return "";
        }

        if (nearestSnapshot.getVersion() == version) {
            return nearestSnapshot.getContent();
        }

        List<GdocOperationLog> logs = getOperationLogs(docId, nearestSnapshot.getVersion(), version);
        String content = nearestSnapshot.getContent();

        for (GdocOperationLog logEntry : logs) {
            content = applyLogToContent(content, logEntry);
        }

        return content;
    }

    private GdocSnapshot findNearestSnapshot(Long docId, int version) {
        LambdaQueryWrapper<GdocSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GdocSnapshot::getDocId, docId)
                .le(GdocSnapshot::getVersion, version)
                .orderByDesc(GdocSnapshot::getVersion)
                .last("LIMIT 1");
        return snapshotMapper.selectOne(wrapper);
    }

    private String applyLogToContent(String content, GdocOperationLog logEntry) {
        return content;
    }

    public int getOperationCount(Long docId) {
        LambdaQueryWrapper<GdocOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GdocOperationLog::getDocId, docId);
        return Math.toIntExact(operationLogMapper.selectCount(wrapper));
    }

    public List<Map<String, Object>> compareVersions(Long docId, int versionA, int versionB) {
        String contentA = getContentAtVersion(docId, versionA);
        String contentB = getContentAtVersion(docId, versionB);

        if (contentA.equals(contentB)) {
            return List.of();
        }

        String[] linesA = contentA.split("\n");
        String[] linesB = contentB.split("\n");

        List<Map<String, Object>> diffs = new ArrayList<>();
        int[][] matrix = buildLCSTable(linesA, linesB);
        buildDiff(diffs, linesA, linesB, matrix, linesA.length, linesB.length);

        return diffs;
    }

    private int[][] buildLCSTable(String[] a, String[] b) {
        int m = a.length;
        int n = b.length;
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a[i - 1].equals(b[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp;
    }

    private void buildDiff(List<Map<String, Object>> diffs, String[] a, String[] b, int[][] dp, int i, int j) {
        if (i > 0 && j > 0 && a[i - 1].equals(b[j - 1])) {
            buildDiff(diffs, a, b, dp, i - 1, j - 1);
            diffs.add(Map.of("type", "unchanged", "line", i - 1, "text", a[i - 1]));
        } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
            buildDiff(diffs, a, b, dp, i, j - 1);
            diffs.add(Map.of("type", "added", "line", j - 1, "text", b[j - 1]));
        } else if (i > 0 && (j == 0 || dp[i][j - 1] < dp[i - 1][j])) {
            buildDiff(diffs, a, b, dp, i - 1, j);
            diffs.add(Map.of("type", "removed", "line", i - 1, "text", a[i - 1]));
        }
    }
}
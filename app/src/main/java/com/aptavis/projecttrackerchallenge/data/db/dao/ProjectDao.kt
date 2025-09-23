package com.aptavis.projecttrackerchallenge.data.db.dao

import androidx.room.*
import com.aptavis.projecttrackerchallenge.data.db.entity.ProjectEntity
import com.aptavis.projecttrackerchallenge.domain.model.ProjectComputed
import com.aptavis.projecttrackerchallenge.domain.model.Status
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("""
        SELECT
          p.id AS id,
          p.name AS name,
          p.status AS status,
          CASE 
            WHEN COALESCE(SUM(t.weight), 0) = 0 THEN 0
            ELSE CAST( (100 * COALESCE(SUM(CASE WHEN t.status = 'Done' THEN t.weight ELSE 0 END), 0)) 
                       / COALESCE(SUM(t.weight), 0) AS INT)
          END AS progress,
          COUNT(t.id) AS taskCount,
          COALESCE(SUM(
            CASE 
              WHEN t.status != 'Done' 
               AND t.deadlineAt IS NOT NULL 
               AND t.deadlineAt <= :soonUntilMs 
              THEN 1 ELSE 0 
            END
          ), 0) AS deadlineSoonCount
        FROM projects p
        LEFT JOIN tasks t ON t.projectId = p.id
        GROUP BY p.id
        ORDER BY p.id DESC
    """)
    fun observeProjectsComputed(
        soonUntilMs: Long = System.currentTimeMillis() + 24*60*60*1000
    ): kotlinx.coroutines.flow.Flow<List<com.aptavis.projecttrackerchallenge.domain.model.ProjectComputed>>


    @Query("""
      SELECT
        CASE
          WHEN totalCnt = 0 THEN :draft                         -- tidak ada task → Draft
          WHEN doneCnt = totalCnt THEN :done                    -- semua Done → Done
          WHEN doneCnt = 0 AND inprogCnt = 0 THEN :draft        -- semua Draft → Draft
          WHEN inprogCnt > 0 THEN :inprog                       -- ada yg InProgress → InProgress
          ELSE :inprog                                          -- campuran Draft+Done (belum complete) → InProgress
        END
      FROM (
        SELECT
          COUNT(*) AS totalCnt,
          COALESCE(SUM(CASE WHEN status = 'Done' THEN 1 ELSE 0 END), 0)        AS doneCnt,
          COALESCE(SUM(CASE WHEN status = 'InProgress' THEN 1 ELSE 0 END), 0)  AS inprogCnt
        FROM tasks
        WHERE projectId = :projectId
      ) AS agg
    """)
    suspend fun deriveStatusFromTasks(
        projectId: Long,
        draft: Status = Status.Draft,
        inprog: Status = Status.InProgress,
        done: Status = Status.Done
    ): Status



    @Query("UPDATE projects SET status = :status, updatedAt = :updatedAt WHERE id = :projectId")
    suspend fun setProjectStatus(projectId: Long, status: Status, updatedAt: Long = System.currentTimeMillis())

    @Insert suspend fun insert(p: ProjectEntity): Long
    @Update suspend fun update(p: ProjectEntity)
    @Delete suspend fun delete(p: ProjectEntity)

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): ProjectEntity?
}

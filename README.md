# Project Tracker Challenge (Android)

Aplikasi Android sederhana untuk melacak **project** dan **task** dengan perhitungan progress otomatis untuk ngelamar k*rja

---


## Profil

**Izamul Fikri**  
22 Tahun

---


## ✅ Checklist Fitur

- [x] **Add Project** (dialog)
- [x] **Add Task** (dialog + pilih project via dialog chooser)
- [x] **Daftar Project** (card)
    - [x] Expand/Collapse menampilkan **Task 1, Task 2, …**
    - [x] Tombol **+** di tiap project untuk quick add task
    - [x] Klik card → **Edit Project** (nama) + **Hapus / Simpan**
- [x] **Dialog Add/Edit Project/Task**
    - [x] Field Project/Task: **Nama**, **Status** (`Draft`, `InProgress`, `Done`)
    - [x] Task punya **Weight (1–10)**, opsional **Deadline**
    - [x] Animasi **slide in/out** saat buka/tutup dialog
- [x] **Penghitungan Progress Project**
    - Progress = (∑ bobot task berstatus `Done`) / (∑ bobot semua task) × 100%
- [x] **Status Project otomatis** (tidak bisa diubah manual)
    - Tidak ada task → **Draft**
    - Semua task **Draft** → **Draft**
    - Ada task **InProgress** → **InProgress**
    - Semua task **Done** → **Done**
    - Campuran Draft+Done (belum selesai total) → **InProgress**
- [x] **Arsitektur Multi-Fragment** dalam satu Activity
- [x] **Realtime update** setelah create/update/delete (Room Flow → LiveData)
- [x] **Persistensi** dengan **Room** (SQLite)


---

## Stack & Modul Utama

- **Kotlin**, **Coroutines**
- **Room** (SQLite) + Flow
- **Hilt** (Dependency Injection)
- **ViewModel**, **LiveData**
- **ViewBinding**, **Material Components**
- **Navigation** (single-activity, multi-fragment)

Struktur (ringkas):
```
data/
  db/ { AppDb, dao/, entity/ }
  repository/ ProjectRepository
domain/
  model/ { ProjectComputed, Status }
di/
  { DatabaseModule, RepositoryModule, CoroutinesModule }
ui/
  home/ { HomeFragment, HomeViewModel, ProjectExpandableAdapter }
  dialog/ { ProjectDialogFragment, TaskDialogFragment, ChooseProjectDialogFragment }
  common/ { StatusUi, ... }
prefs/ { ThemePrefs (opsional) }
```

---

## Lingkungan Pengembangan & Konfigurasi

1. **Software**: Android Studio (Koala), JDK 17, minSdk 29 (disarankan 34+).
2. **Clone** repo ini lalu buka di Android Studio.
3. **Sync Gradle** → **Run** ke emulator/perangkat.

> Tidak ada konfigurasi khusus/rahasia. Database Room tersimpan lokal di device/emulator.

---

## Arsitektur & Alur Data

```
Room (Flow) ──> Repository (asLiveData) ──> ViewModel ──> Fragment (observe)
```

- DAO mengeluarkan **Flow** (hemat & reaktif).
- Repository mengubah jadi **LiveData** (`asLiveData()`) supaya mudah di-observe dari Fragment.
- Setiap operasi Task (add/update/delete) memanggil `recalcProjectStatus(projectId)` → status project otomatis tersinkron.

---

## Model Data (intisari)

```kotlin
// Project
@Entity(tableName = "projects")
data class ProjectEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val status: Status = Status.Draft,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

// Task
@Entity(
  tableName = "tasks",
  foreignKeys = [ForeignKey(
     entity = ProjectEntity::class, parentColumns = ["id"],
     childColumns = ["projectId"], onDelete = ForeignKey.CASCADE
  )],
  indices = [Index("projectId"), Index("status"), Index("deadlineAt")]
)
data class TaskEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val projectId: Long,
  val name: String,
  val weight: Int = 1,                // 1..10
  val status: Status = Status.Draft,
  val deadlineAt: Long? = null,
  val notifyEnabled: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
```

**Status Project otomatis (SQL)**
```sql
CASE
  WHEN totalCnt = 0 THEN :draft
  WHEN doneCnt = totalCnt THEN :done
  WHEN doneCnt = 0 AND inprogCnt = 0 THEN :draft
  WHEN inprogCnt > 0 THEN :inprog
  ELSE :inprog
END
```

---

## Kenapa Hilt?

- Kita butuh cara **mendistribusikan dependensi** (DB, repo, dispatcher) ke banyak kelas **tanpa wiring manual** di setiap tempat.
- **Hilt** = colokan standar Android: deklarasi sekali, lalu tinggal `@Inject`.
- Dampaknya: **struktur lebih rapi & gampang diskalakan**. Nambah layar/fitur nggak bikin DI berantakan.
- Trade-off: ada “ritual” anotasi.

> Intinya: sebenernya saya belajarnya emang pakai hilt jadi pahamnya begini 😁

---



## Demo (Video)

Download demo: **[demo_app.mp4](demo/demo_app.mp4)**

---

## Lisensi

Bebas digunakan untuk pembelajaran/pengembangan internal. 
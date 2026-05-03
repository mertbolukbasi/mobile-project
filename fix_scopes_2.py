import re

file_path = "app/src/main/java/com/example/paginex/UiScreens.kt"
with open(file_path, "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "newIsSaved" in line and "Unresolved reference" in line:
        pass # this was compiler output not the file

# Let's just fix it by regex since I know the pattern
with open(file_path, "r") as f:
    content = f.read()

# Pattern for toggleBookListSave missing newIsSaved assignment
content = content.replace("""
                                            val index = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                            if (index != -1) {
                                                val currentList = MockData.sampleBookLists[index]
                                                MockData.sampleBookLists[index] = currentList.copy(
                                                    isSaved = !currentList.isSaved
                                                )
                                            }
                                            kotlinx.coroutines.MainScope().launch {
                                                FirestoreService.toggleBookListSave(list.id, AuthService.getUid(), newIsSaved)
                                            }""", """
                                            val index = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                            val newIsSaved = if (index != -1) !MockData.sampleBookLists[index].isSaved else !list.isSaved
                                            if (index != -1) {
                                                val currentList = MockData.sampleBookLists[index]
                                                MockData.sampleBookLists[index] = currentList.copy(
                                                    isSaved = newIsSaved
                                                )
                                            }
                                            kotlinx.coroutines.MainScope().launch {
                                                FirestoreService.toggleBookListSave(list.id, AuthService.getUid(), newIsSaved)
                                            }""")

content = content.replace("""
                                    val index = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                    if (index != -1) {
                                        val currentList = MockData.sampleBookLists[index]
                                        MockData.sampleBookLists[index] = currentList.copy(
                                            isSaved = !currentList.isSaved
                                        )
                                    }
                                    kotlinx.coroutines.MainScope().launch {
                                        FirestoreService.toggleBookListSave(list.id, currentUid, newIsSaved)
                                    }""", """
                                    val index = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                    val newIsSaved = if (index != -1) !MockData.sampleBookLists[index].isSaved else !list.isSaved
                                    if (index != -1) {
                                        val currentList = MockData.sampleBookLists[index]
                                        MockData.sampleBookLists[index] = currentList.copy(
                                            isSaved = newIsSaved
                                        )
                                    }
                                    kotlinx.coroutines.MainScope().launch {
                                        FirestoreService.toggleBookListSave(list.id, currentUid, newIsSaved)
                                    }""")

with open(file_path, "w") as f:
    f.write(content)

print("Scopes 2 fixed.")

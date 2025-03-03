package com.balugaq.sfworldedit.api.data;

import com.balugaq.sfworldedit.implementation.SFWorldedit;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Backup {
    private static final int MAX_BACKUP_SIZE = SFWorldedit.getInstance().getConfigManager().getMaxBackups();
    private final UUID playerUUID;
    @Nonnull
    private final List<List<Content>> backup;
    private int pointer = 0;

    public Backup(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.backup = new ArrayList<>();
    }

    public List<Content> getContent(int index) {
        if (backup.isEmpty()) {
            return new ArrayList<>();
        }

        if (index < 0 || index >= backup.size()) {
            return new ArrayList<>();
        }

        refresh();

        return backup.get(index);
    }

    public List<Content> getRightContent(List<Content> callback) {
        if (backup.isEmpty()) {
            return new ArrayList<>();
        }

        if (pointer + 1 > backup.size()) {
            return callback;
        }

        refresh();

        return backup.get(pointer + 1);
    }

    public List<Content> getLeftContent(List<Content> callback) {
        if (backup.isEmpty()) {
            return new ArrayList<>();
        }

        if (pointer - 1 < 0) {
            return callback;
        }

        refresh();

        return backup.get(pointer - 1);
    }

    public List<Content> getRightContentAndIncreasePointer(List<Content> callback) {
        if (backup.isEmpty()) {
            return new ArrayList<>();
        }

        if (pointer + 1 > backup.size()) {
            return callback;
        }

        refresh();

        return backup.get(pointer++);
    }

    public List<Content> getLeftContentAndDecreasePointer(List<Content> callback) {
        if (backup.isEmpty()) {
            return new ArrayList<>();
        }

        if (pointer - 1 < 0) {
            return callback;
        }

        refresh();

        return backup.get(--pointer);
    }

    public void addContent(List<Content> content) {
        if (backup.size() == pointer) {
            backup.add(new ArrayList<>());
        }

        refresh();

        backup.set(pointer++, content);
    }

    public void removeContent() {
        backup.remove(pointer);
        pointer--;
        refresh();
    }

    public void clear() {
        backup.clear();
        pointer = 0;
    }

    public boolean isEmpty() {
        return backup.isEmpty();
    }

    public int size() {
        return backup.size();
    }


    @Nonnull
    public List<List<Content>> getAllBackup() {
        return backup;
    }

    public int decreasePointer() {
        return pointer--;
    }

    public int increasePointer() {
        return pointer++;
    }

    public void refresh() {
        if (backup.size() > MAX_BACKUP_SIZE) {
            backup.remove(0);
            for (int i = 0; i < pointer - 1; i++) {
                backup.set(i, backup.get(i + 1));
            }
            pointer--;
        }
    }
}

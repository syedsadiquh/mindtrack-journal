package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.journal.dto.request.CreateBlockRequest;
import com.syedsadiquh.coreservice.journal.dto.request.ReorderBlocksRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateBlockRequest;
import com.syedsadiquh.coreservice.journal.dto.response.JournalBlockResponse;

import java.util.List;
import java.util.UUID;

public interface JournalBlockService {

    JournalBlockResponse addBlock(UUID userId, UUID pageId, CreateBlockRequest request);

    JournalBlockResponse updateBlock(UUID userId, UUID pageId, UUID blockId, UpdateBlockRequest request);

    void softDeleteBlock(UUID userId, UUID pageId, UUID blockId);

    List<JournalBlockResponse> reorderBlocks(UUID userId, UUID pageId, ReorderBlocksRequest request);
}

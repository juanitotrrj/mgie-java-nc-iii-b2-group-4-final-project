package com.group4.inventoryserver.dto.icr;

public class IcrDetailData extends IcrData {

  private final ProofFileData proofFile;

  public IcrDetailData(
      long requestId,
      String requestNo,
      long productId,
      String productCode,
      String productName,
      String requestType,
      int currentQuantity,
      Integer requestedQuantity,
      Integer quantityChange,
      String reason,
      String status,
      long requestedById,
      String requestedBy,
      String requestedAt,
      String reviewedBy,
      String reviewedAt,
      String reviewNotes,
      ProofFileData proofFile) {
    super(
        requestId,
        requestNo,
        productId,
        productCode,
        productName,
        requestType,
        currentQuantity,
        requestedQuantity,
        quantityChange,
        reason,
        status,
        requestedById,
        requestedBy,
        requestedAt,
        reviewedBy,
        reviewedAt,
        reviewNotes);
    this.proofFile = proofFile;
  }

  public ProofFileData getProofFile() {
    return proofFile;
  }

  public static class ProofFileData {
    private final long fileId;
    private final String originalFilename;
    private final String mimeType;
    private final long sizeBytes;
    private final String downloadUrl;

    public ProofFileData(
        long fileId, String originalFilename, String mimeType, long sizeBytes, String downloadUrl) {
      this.fileId = fileId;
      this.originalFilename = originalFilename;
      this.mimeType = mimeType;
      this.sizeBytes = sizeBytes;
      this.downloadUrl = downloadUrl;
    }

    public long getFileId() {
      return fileId;
    }

    public String getOriginalFilename() {
      return originalFilename;
    }

    public String getMimeType() {
      return mimeType;
    }

    public long getSizeBytes() {
      return sizeBytes;
    }

    public String getDownloadUrl() {
      return downloadUrl;
    }
  }
}

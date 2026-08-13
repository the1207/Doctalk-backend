package com.Doctalk.Doctalk_backend.service.implementation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.Doctalk.Doctalk_backend.Mapper.DocumentsMapper;
import com.Doctalk.Doctalk_backend.dto.reponse.DocumentReponse;
import com.Doctalk.Doctalk_backend.dto.request.DocumentRequest;
import com.Doctalk.Doctalk_backend.entities.Chunk;
import com.Doctalk.Doctalk_backend.entities.Document;
import com.Doctalk.Doctalk_backend.entities.Folder;
import com.Doctalk.Doctalk_backend.entities.Tag;
import com.Doctalk.Doctalk_backend.repository.ChunkRepository;
import com.Doctalk.Doctalk_backend.repository.DocumentsRepository;
import com.Doctalk.Doctalk_backend.repository.FolderRepository;
import com.Doctalk.Doctalk_backend.repository.TagRepository;
import com.Doctalk.Doctalk_backend.service.ChunkingService;
import com.Doctalk.Doctalk_backend.service.DocumentService;
import com.Doctalk.Doctalk_backend.service.EmbeddingService;
import com.Doctalk.Doctalk_backend.service.PdfParsingService;
import com.Doctalk.Doctalk_backend.utils.VectorUtils;

import jakarta.transaction.Transactional;

@Service
public class DocumentImplementation implements DocumentService {

    private final DocumentsRepository documentsRepository;
    private final DocumentsMapper documentsMapper;
    private final PdfParsingService pdfParsingService;
    private final TagRepository tagRepository;
    private final FolderRepository folderRepository;
    private final ChunkRepository chunkRepository;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;

    public DocumentImplementation(DocumentsRepository documentsRepository,
                               DocumentsMapper documentsMapper,
                               PdfParsingService pdfParsingService, TagRepository tagRepository, FolderRepository folderRepository,
                                  ChunkRepository chunkRepository,ChunkingService chunkingService,EmbeddingService embeddingService) {  // ← AJOUTÉ
        this.documentsRepository = documentsRepository;
        this.documentsMapper = documentsMapper;
        this.pdfParsingService = pdfParsingService;
        this.tagRepository = tagRepository;
        this.folderRepository = folderRepository;
        this.chunkRepository = chunkRepository;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
    }

    // Créer un document sans parsing automatique (pour les tests)
    @Override
    public DocumentReponse create(DocumentRequest request) {
        Document document = documentsMapper.toEntity(request);
        document = documentsRepository.save(document);
        return documentsMapper.toResponse(document);
    }
    @Override
    public DocumentReponse update(Long id, DocumentRequest request) {
        Document existingDocument = documentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'id : " + id));

        existingDocument.setTitle(request.title());
        existingDocument.setFilename(request.filename());
        existingDocument.setFilePath(request.filePath());
        existingDocument.setFileSize(request.fileSize());
        existingDocument.setMimeType(request.mimeType());
        existingDocument.setContent(request.content());
        existingDocument.setStatus(request.status());

        Document updatedDocument = documentsRepository.save(existingDocument);
        return documentsMapper.toResponse(updatedDocument);
    }
    @Override
    public DocumentReponse getById(Long id) {
        Document document = documentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'id : " + id));
        return documentsMapper.toResponse(document);
    }

    @Override
    public List<DocumentReponse> getAll() {
        return documentsRepository.findAll()
                .stream()
                .map(documentsMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!documentsRepository.existsById(id)) {
            throw new RuntimeException("Document non trouvé avec l'id : " + id);
        }
        documentsRepository.deleteById(id);
    }
    @Override
    @Transactional
    public DocumentReponse addTagToDocument(Long documentId, Long tagId) {
        Document document = documentsRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag non trouvé"));

        document.getTags().add(tag);
        Document updated = documentsRepository.save(document);
        return documentsMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public DocumentReponse removeTagFromDocument(Long documentId, Long tagId) {
        Document document = documentsRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag non trouvé"));

        document.getTags().remove(tag);
        Document updated = documentsRepository.save(document);
        return documentsMapper.toResponse(updated);
    }

    // Pareil pour Folder
    @Override
    @Transactional
    public DocumentReponse addFolderToDocument(Long documentId, Long folderId) {
        Document document = documentsRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));

        document.getFolders().add(folder);
        Document updated = documentsRepository.save(document);
        return documentsMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public DocumentReponse removeFolderFromDocument(Long documentId, Long folderId) {
        Document document = documentsRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));

        document.getFolders().remove(folder);
        Document updated = documentsRepository.save(document);
        return documentsMapper.toResponse(updated);
    }
    @Override
    public List<DocumentReponse> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of(); // Retourne une liste vide si la recherche est vide
        }

        List<Document> documents = documentsRepository.searchByKeyword(keyword.trim());
        return documents.stream()
                .map(documentsMapper::toResponse)
                .collect(Collectors.toList());
    }
    private void generateChunksForDocument(Document document) {
        String content = document.getContent();
        if (content == null || content.isEmpty()) {
            return;
        }

        // 1. Découper le texte en chunks
        List<String> chunks = chunkingService.chunkText(content);

        // 2. Supprimer les anciens chunks si existants
        chunkRepository.deleteByDocumentId(document.getId());

        // 3. Créer et sauvegarder les nouveaux chunks
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = new Chunk();
            chunk.setContent(chunks.get(i));
            chunk.setChunkIndex(i);
            chunk.setDocument(document);

            // Générer l'embedding pour chaque chunk
            try {
                List<Float> embedding = embeddingService.generateEmbedding(chunks.get(i));
                chunk.setEmbedding(VectorUtils.toPgVectorLiteral(embedding));
            } catch (Exception e) {
                chunk.setEmbedding("[]"); // Embedding vide en cas d'erreur
            }

            chunkRepository.save(chunk);
        }

    }
    @Override
    public List<DocumentReponse> searchSemantic(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Générer l'embedding de la requête
        List<Float> queryEmbedding = embeddingService.generateEmbedding(query);

        // 2. Convertir en format PostgreSQL (ex: [0.1, 0.2, 0.3, ...])
        String vectorString = VectorUtils.toPgVectorLiteral(queryEmbedding);

        // 3. Chercher les chunks similaires (tous documents confondus)
        List<Chunk> similarChunks = chunkRepository.findSimilarChunksAll(vectorString, 20);

        // 4. Récupérer les documents uniques à partir des chunks
        Set<Document> documents = new HashSet<>();
        for (Chunk chunk : similarChunks) {
            documents.add(chunk.getDocument());
        }

        // 5. Transformer en DocumentReponse
        List<DocumentReponse> responses = new ArrayList<>();
        for (Document doc : documents) {
            responses.add(documentsMapper.toResponse(doc));
        }

        return responses;
    }
    // NOUVELLE MÉTHODE : Créer un document avec parsing automatique
    @Override
    public DocumentReponse createWithParsing(DocumentRequest request, MultipartFile file) {
        Document document = documentsMapper.toEntity(request);

        try {
            String extractedText = pdfParsingService.extractText(file);
            if (extractedText != null && !extractedText.isBlank()) {
                document.setContent(extractedText.trim());
                document.setStatus("PARSED");
            } else {
                document.setContent("");
                document.setStatus("ERROR");
            }
        } catch (Exception e) {
            document.setContent("Erreur d'extraction : " + e.getMessage());
            document.setStatus("ERROR");
        }

        document = documentsRepository.save(document);

        if ("PARSED".equals(document.getStatus())) {
            generateChunksForDocument(document);
            document.setStatus("VECTORIZED");
            document = documentsRepository.save(document);
        }

        return documentsMapper.toResponse(document);
    }

}
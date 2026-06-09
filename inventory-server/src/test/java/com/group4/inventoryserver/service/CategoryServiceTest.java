package com.group4.inventoryserver.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.category.CategoryCreateRequest;
import com.group4.inventoryserver.dto.category.CategoryData;
import com.group4.inventoryserver.dto.category.CategoryUpdateRequest;
import com.group4.inventoryserver.exception.ConflictException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.CategoryRepository;
import com.group4.inventoryserver.server.RequestContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class CategoryServiceTest {

  private CategoryRepository categoryRepository;
  private CategoryService categoryService;

  @Before
  public void setUp() {
    categoryRepository = mock(CategoryRepository.class);
    categoryService = new CategoryService(categoryRepository);
  }

  private CategoryData sampleCategory() {
    return new CategoryData(
        1L,
        "CAT001",
        "Electronics",
        "Electronic devices and accessories",
        "Product Group",
        5,
        "Active",
        "2026-01-01T00:00:00Z",
        "2026-01-01T00:00:00Z");
  }

  private CategoryCreateRequest validCreateRequest() {
    CategoryCreateRequest req = new CategoryCreateRequest();
    req.setCategoryName("New Category");
    return req;
  }

  // ─── create() ─────────────────────────────────────────────────────────────────

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenNameBlank() {
    CategoryCreateRequest req = new CategoryCreateRequest();
    req.setCategoryName("");
    categoryService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenNameNull() {
    CategoryCreateRequest req = new CategoryCreateRequest();
    categoryService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenNameTooLong() {
    CategoryCreateRequest req = new CategoryCreateRequest();
    StringBuilder longName = new StringBuilder();
    for (int i = 0; i < 130; i++) longName.append('a');
    req.setCategoryName(longName.toString());
    categoryService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenDescriptionTooLong() {
    CategoryCreateRequest req = validCreateRequest();
    StringBuilder longDesc = new StringBuilder();
    for (int i = 0; i < 510; i++) longDesc.append('a');
    req.setDescription(longDesc.toString());
    categoryService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenTypeTooLong() {
    CategoryCreateRequest req = validCreateRequest();
    StringBuilder longType = new StringBuilder();
    for (int i = 0; i < 90; i++) longType.append('a');
    req.setType(longType.toString());
    categoryService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenStatusInvalid() {
    CategoryCreateRequest req = validCreateRequest();
    req.setStatus("Archived");
    categoryService.create(req, 1L);
  }

  @Test(expected = ConflictException.class)
  public void create_throwsConflict_whenNameExists() {
    CategoryCreateRequest req = validCreateRequest();
    when(categoryRepository.existsByName("New Category")).thenReturn(true);
    categoryService.create(req, 1L);
  }

  @Test
  public void create_success_insertsAndReturnsData() {
    CategoryCreateRequest req = validCreateRequest();
    req.setDescription("A description");
    when(categoryRepository.existsByName("New Category")).thenReturn(false);
    when(categoryRepository.generateNextCode()).thenReturn("CAT005");
    when(categoryRepository.insert(
            eq("CAT005"),
            eq("New Category"),
            eq("A description"),
            eq("Product Group"),
            eq("Active"),
            eq(1L)))
        .thenReturn(5L);
    when(categoryRepository.findById(5L))
        .thenReturn(
            new CategoryData(
                5L,
                "CAT005",
                "New Category",
                "A description",
                "Product Group",
                0,
                "Active",
                "2026-01-01T00:00:00Z",
                "2026-01-01T00:00:00Z"));

    CategoryData result = categoryService.create(req, 1L);
    assertNotNull(result);
    assertEquals("CAT005", result.getCategoryCode());
    assertEquals("New Category", result.getCategoryName());
  }

  @Test
  public void create_usesDefaultTypeAndStatus_whenNotProvided() {
    CategoryCreateRequest req = validCreateRequest();
    when(categoryRepository.existsByName("New Category")).thenReturn(false);
    when(categoryRepository.generateNextCode()).thenReturn("CAT005");
    when(categoryRepository.insert(
            anyString(), anyString(), any(), eq("Product Group"), eq("Active"), anyLong()))
        .thenReturn(5L);
    when(categoryRepository.findById(5L)).thenReturn(sampleCategory());

    categoryService.create(req, 1L);

    verify(categoryRepository)
        .insert(anyString(), anyString(), any(), eq("Product Group"), eq("Active"), anyLong());
  }

  @Test
  public void create_usesProvidedTypeAndStatus() {
    CategoryCreateRequest req = validCreateRequest();
    req.setType("Service Group");
    req.setStatus("Inactive");
    when(categoryRepository.existsByName("New Category")).thenReturn(false);
    when(categoryRepository.generateNextCode()).thenReturn("CAT005");
    when(categoryRepository.insert(
            anyString(), anyString(), any(), eq("Service Group"), eq("Inactive"), anyLong()))
        .thenReturn(5L);
    when(categoryRepository.findById(5L)).thenReturn(sampleCategory());

    categoryService.create(req, 1L);

    verify(categoryRepository)
        .insert(anyString(), anyString(), any(), eq("Service Group"), eq("Inactive"), anyLong());
  }

  // ─── getById() ────────────────────────────────────────────────────────────────

  @Test
  public void getById_returnsCategory_whenExists() {
    when(categoryRepository.findById(1L)).thenReturn(sampleCategory());
    CategoryData result = categoryService.getById(1L);
    assertNotNull(result);
    assertEquals("CAT001", result.getCategoryCode());
    assertEquals("Electronics", result.getCategoryName());
  }

  @Test(expected = NotFoundException.class)
  public void getById_throwsNotFound_whenMissing() {
    when(categoryRepository.findById(99L)).thenReturn(null);
    categoryService.getById(99L);
  }

  // ─── update() ─────────────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void update_throwsNotFound_whenCategoryMissing() {
    when(categoryRepository.findById(99L)).thenReturn(null);
    categoryService.update(99L, new CategoryUpdateRequest(), 1L);
  }

  @Test(expected = ConflictException.class)
  public void update_throwsConflict_whenNameAlreadyUsed() {
    when(categoryRepository.findById(1L)).thenReturn(sampleCategory());
    CategoryUpdateRequest req = new CategoryUpdateRequest();
    req.setCategoryName("Existing Name");
    when(categoryRepository.existsByNameExcluding("Existing Name", 1L)).thenReturn(true);
    categoryService.update(1L, req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void update_throwsValidation_whenNameTooLong() {
    when(categoryRepository.findById(1L)).thenReturn(sampleCategory());
    CategoryUpdateRequest req = new CategoryUpdateRequest();
    StringBuilder longName = new StringBuilder();
    for (int i = 0; i < 130; i++) longName.append('a');
    req.setCategoryName(longName.toString());
    categoryService.update(1L, req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void update_throwsValidation_whenStatusInvalid() {
    when(categoryRepository.findById(1L)).thenReturn(sampleCategory());
    CategoryUpdateRequest req = new CategoryUpdateRequest();
    req.setStatus("Archived");
    categoryService.update(1L, req, 1L);
  }

  @Test
  public void update_success_updatesAndReturnsCategory() {
    when(categoryRepository.findById(1L)).thenReturn(sampleCategory());
    CategoryUpdateRequest req = new CategoryUpdateRequest();
    req.setCategoryName("Updated Name");
    req.setDescription("New desc");
    when(categoryRepository.existsByNameExcluding("Updated Name", 1L)).thenReturn(false);
    when(categoryRepository.findById(1L))
        .thenReturn(
            new CategoryData(
                1L,
                "CAT001",
                "Updated Name",
                "New desc",
                "Product Group",
                5,
                "Active",
                "2026-01-01T00:00:00Z",
                "2026-01-02T00:00:00Z"));

    CategoryData result = categoryService.update(1L, req, 1L);
    assertNotNull(result);
    verify(categoryRepository)
        .update(eq(1L), eq("Updated Name"), eq("New desc"), isNull(), isNull(), eq(1L));
  }

  @Test
  public void update_doesNotCheckNameConflict_whenNameNotProvided() {
    when(categoryRepository.findById(1L)).thenReturn(sampleCategory());
    CategoryUpdateRequest req = new CategoryUpdateRequest();
    req.setDescription("Only updating description");
    when(categoryRepository.findById(1L)).thenReturn(sampleCategory());

    categoryService.update(1L, req, 1L);

    verify(categoryRepository, never()).existsByNameExcluding(anyString(), anyLong());
    verify(categoryRepository)
        .update(eq(1L), isNull(), eq("Only updating description"), isNull(), isNull(), eq(1L));
  }

  // ─── deactivate() ─────────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void deactivate_throwsNotFound_whenMissing() {
    when(categoryRepository.findById(99L)).thenReturn(null);
    categoryService.deactivate(99L, 1L);
  }

  @Test
  public void deactivate_success() {
    when(categoryRepository.findById(1L)).thenReturn(sampleCategory());
    categoryService.deactivate(1L, 1L);
    verify(categoryRepository).deactivate(1L, 1L);
  }

  // ─── list() ───────────────────────────────────────────────────────────────────

  @Test
  public void list_returnsPaginatedResponse() {
    List<CategoryData> categories = Arrays.asList(sampleCategory());
    when(categoryRepository.findAll(0, 10, "categoryName", "asc", null, null, null))
        .thenReturn(categories);
    when(categoryRepository.count(null, null, null)).thenReturn(1L);

    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(null);
    when(ctx.getQueryParam("size")).thenReturn(null);
    when(ctx.getQueryParam("sortBy", "categoryName")).thenReturn("categoryName");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("asc");
    when(ctx.getQueryParam("search")).thenReturn(null);
    PaginationParams params =
        PaginationParams.from(ctx, "categoryName", new HashSet<>(Arrays.asList("categoryName")));

    PaginatedResponse<CategoryData> result = categoryService.list(params, null, null);
    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    assertEquals(1L, result.getMeta().getTotalRecords());
  }

  @Test
  public void list_filtersReturnEmptyList() {
    when(categoryRepository.findAll(0, 10, "categoryName", "asc", null, "Inactive", null))
        .thenReturn(Collections.<CategoryData>emptyList());
    when(categoryRepository.count(null, "Inactive", null)).thenReturn(0L);

    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(null);
    when(ctx.getQueryParam("size")).thenReturn(null);
    when(ctx.getQueryParam("sortBy", "categoryName")).thenReturn("categoryName");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("asc");
    when(ctx.getQueryParam("search")).thenReturn(null);
    PaginationParams params =
        PaginationParams.from(ctx, "categoryName", new HashSet<>(Arrays.asList("categoryName")));

    PaginatedResponse<CategoryData> result = categoryService.list(params, "Inactive", null);
    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
    assertEquals(0L, result.getMeta().getTotalRecords());
  }
}

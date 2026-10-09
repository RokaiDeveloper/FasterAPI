package core;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


public class GenericCrudController<T> {

    private final GenericCrudService<T> service;
    private final String basePath;
    private final boolean enableGet;
    private final boolean enablePost;
    private final boolean enablePut;
    private final boolean enablePatch;
    private final boolean enableDelete;

    public GenericCrudController(GenericCrudService<T> service, String basePath) {
        this(service, basePath, true, true, true, true, true);
    }

    public GenericCrudController(GenericCrudService<T> service, String basePath,
                                  boolean enableGet, boolean enablePost, boolean enablePut,
                                  boolean enablePatch, boolean enableDelete) {
        this.service = service;
        this.basePath = basePath;
        this.enableGet = enableGet;
        this.enablePost = enablePost;
        this.enablePut = enablePut;
        this.enablePatch = enablePatch;
        this.enableDelete = enableDelete;
    }

    @GetMapping
    @ResponseBody
    public Object findAll(@RequestParam(name = "page", required = false) Integer page,
                          @RequestParam(name = "size", required = false) Integer size,
                          @RequestParam(name = "sort", required = false) String sort,
                          @RequestParam Map<String, String> allParams) {
        if (!enableGet) {
            throw new UnsupportedOperationException("Endpoint GET is disabled");
        }
        // Remove pagination params from filters
        Map<String, String> filters = new java.util.HashMap<>(allParams);
        filters.remove("page");
        filters.remove("size");
        filters.remove("sort");
        
        if (page != null || size != null || sort != null) {
            org.springframework.data.domain.Pageable pageable = buildPageable(page, size, sort);
            if (!filters.isEmpty()) {
                return service.findAll(filters, pageable);
            }
            return service.findAll(pageable);
        }
        
        if (!filters.isEmpty()) {
            return service.findAll(filters);
        }
        
        return service.findAll();
    }

    private org.springframework.data.domain.Pageable buildPageable(Integer page, Integer size, String sort) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        
        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            org.springframework.data.domain.Sort.Direction direction = 
                sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") 
                ? org.springframework.data.domain.Sort.Direction.DESC 
                : org.springframework.data.domain.Sort.Direction.ASC;
            return org.springframework.data.domain.PageRequest.of(pageNum, pageSize, 
                org.springframework.data.domain.Sort.by(direction, sortParams[0]));
        }
        
        return org.springframework.data.domain.PageRequest.of(pageNum, pageSize);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable("id") Long id) {
        if (!enableGet) {
            throw new UnsupportedOperationException("Endpoint GET is disabled");
        }
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        if (!enablePost) {
            throw new UnsupportedOperationException("Endpoint POST is disabled");
        }
        try {
            Object created = service.create(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        if (!enablePut) {
            throw new UnsupportedOperationException("Endpoint PUT is disabled");
        }
        try {
            Object updated = service.update(id, body);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        if (!enablePatch) {
            throw new UnsupportedOperationException("Endpoint PATCH is disabled");
        }
        try {
            Object updated = service.patch(id, body);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (!enableDelete) {
            throw new UnsupportedOperationException("Endpoint DELETE is disabled");
        }
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
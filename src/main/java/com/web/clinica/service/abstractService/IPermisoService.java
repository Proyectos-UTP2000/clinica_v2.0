package com.web.clinica.service.abstractService;

import com.web.clinica.dto.response.PermisoResponse;
import java.util.List;

public interface IPermisoService {

    List<PermisoResponse> listar();
}

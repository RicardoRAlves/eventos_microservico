package utils;

import com.br.capoeira.eventos.organization_api.dto.OrganizationDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitResponseDto;
import com.br.capoeira.eventos.organization_api.model.Organization;
import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;

public class MockUtils {
    public static Organization getMockOrganization() {
        var organization = new Organization();
        organization.setId(1L);
        organization.setName("Grupo Bonfim");
        organization.setSlug("grupo-bonfim");
        organization.setDescription("Grupo de capoeira tradicional");
        organization.setLogoUrl("https://my-bucket.s3.amazonaws.com/logo.jpg");
        organization.setActive(true);
        return organization;
    }

    public static OrganizationDto getMockOrganizationDto() {
        var dto = new OrganizationDto();
        dto.setName("Grupo Bonfim");
        dto.setSlug("grupo-bonfim");
        dto.setDescription("Grupo de capoeira tradicional");
        dto.setLogoUrl("https://my-bucket.s3.amazonaws.com/logo.jpg");
        dto.setActive(true);
        return dto;
    }

    public static OrganizationUnit getMockOrganizationUnit() {
        var unit = new OrganizationUnit();
        unit.setId(1L);
        unit.setOrganization(getMockOrganization());
        unit.setName("Bonfim Jundiaí");
        unit.setSlug("bonfim-jundiai");
        unit.setDescription("Unidade de Jundiaí");
        unit.setCity("Jundiaí");
        unit.setState("SP");
        unit.setCountry("Brasil");
        unit.setAddress("Rua da Capoeira, 123");
        unit.setContactPhone("+55 11 99999-9999");
        unit.setContactEmail("jundiai@bonfim.com");
        unit.setActive(true);
        return unit;
    }

    public static OrganizationUnitDto getMockOrganizationUnitDto() {
        var dto = new OrganizationUnitDto();
        dto.setOrganizationId(1L);
        dto.setName("Bonfim Jundiaí");
        dto.setSlug("bonfim-jundiai");
        dto.setDescription("Unidade de Jundiaí");
        dto.setCity("Jundiaí");
        dto.setState("SP");
        dto.setCountry("Brasil");
        dto.setAddress("Rua da Capoeira, 123");
        dto.setContactPhone("+55 11 99999-9999");
        dto.setContactEmail("jundiai@bonfim.com");
        dto.setActive(true);
        return dto;
    }

    public static OrganizationUnitResponseDto getMockOrganizationUnitResponseDto() {
        var dto = new OrganizationUnitResponseDto();
        dto.setId(1L);
        dto.setOrganizationId(1L);
        dto.setName("Bonfim Jundiaí");
        dto.setSlug("bonfim-jundiai");
        dto.setDescription("Unidade de Jundiaí");
        dto.setCity("Jundiaí");
        dto.setState("SP");
        dto.setCountry("Brasil");
        dto.setAddress("Rua da Capoeira, 123");
        dto.setContactPhone("+55 11 99999-9999");
        dto.setContactEmail("jundiai@bonfim.com");
        dto.setActive(true);
        return dto;
    }
}

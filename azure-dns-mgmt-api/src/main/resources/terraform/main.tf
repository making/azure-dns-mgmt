provider "azurerm" {
  subscription_id = var.subscription_id
  client_id       = var.client_id
  client_secret   = var.client_secret
  tenant_id       = var.tenant_id
  features {
  }
}

resource "azurerm_resource_group" "resource_group" {
  name     = "${var.prefix}-${var.parent_resource_group}"
  location = var.location
}

resource "azurerm_dns_zone" "dns_zone" {
  name                = "${var.prefix}.${var.parent_dns_zone}"
  resource_group_name = azurerm_resource_group.resource_group.name
}


resource "azurerm_dns_ns_record" "ns_record" {
  name                = var.prefix
  zone_name           = var.parent_dns_zone
  resource_group_name = var.parent_resource_group
  ttl                 = 60
  records = azurerm_dns_zone.dns_zone.name_servers
}

output "dns_zone_name" {
  value = azurerm_dns_zone.dns_zone.name
}

output "dns_zone_name_servers" {
  value = azurerm_dns_zone.dns_zone.name_servers
}


package cl.tnt.notifications.service;

import cl.tnt.notifications.dto.PedidoEventDTO;

/** Reglas mínimas de un evento de pedido; un evento inválido lanza IllegalArgumentException. */
final class PedidoValidador {

    private PedidoValidador() {
    }

    static void validar(PedidoEventDTO evento) {
        if (evento == null || evento.getId() == null) {
            throw new IllegalArgumentException("Evento de pedido nulo o sin id.");
        }
        if (evento.getMonto() == null || evento.getMonto() <= 0) {
            throw new IllegalArgumentException(
                    "Pedido " + evento.getId() + " con monto inválido: " + evento.getMonto());
        }
    }
}

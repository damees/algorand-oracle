from pyteal import *


ADMIN_KEY = Bytes("admin")
WHITELISTED_KEY = Bytes("whitelisted")


def approval_program():
    on_creation = Seq(
        [
            Assert(Txn.application_args.length() == Int(0)),
            App.localPut(Int(0), ADMIN_KEY, Int(1)),
            Return(Int(1))
        ]
    )

    is_contract_admin = App.localGet(Int(0), ADMIN_KEY)

    register = Seq(
        [
            App.localPut(Int(0), WHITELISTED_KEY, Int(0)), Return(Int(1))
        ]
    )

    whitelist = Seq(
        [
            Assert(
                And(
                    is_contract_admin,
                    Txn.application_args.length() == Int(2),
                    Txn.accounts.length() == Int(1)
                )
            ),
            App.localPut(Int(1), WHITELISTED_KEY, Int(1)),
            Return(Int(1))
        ]
    )

    is_whitelisted = App.localGet(Int(0), WHITELISTED_KEY)

    market_exchange_rate = Btoi(Txn.application_args[2])  # Value must be provided in micro algo (i.e 1 is 0.000001)
    market = Txn.application_args[1]
    get_market_exchange_rate_callback = Seq(
        [
            Assert(
                And(
                    is_whitelisted,
                    Txn.application_args.length() == Int(3),
                    Txn.accounts.length() == Int(0),
                    )
            ),
            # Do whatever you want with the market value. Here we store it in the global storage.
            # It could be used later by other business methods
            App.globalPut(market, market_exchange_rate),

            Return(Int(1))
        ]
    )

    program = Cond(
        [Txn.application_id() == Int(0), on_creation],
        [Txn.on_completion() == OnComplete.DeleteApplication, Return(is_contract_admin)],
        [Txn.on_completion() == OnComplete.UpdateApplication, Return(is_contract_admin)],
        [Txn.on_completion() == OnComplete.CloseOut, Return(Int(1))],
        [Txn.on_completion() == OnComplete.OptIn, register],
        [Txn.application_args[0] == Bytes("whitelist"), whitelist],
        [Txn.application_args[0] == Bytes("get_market_exchange_rate_callback"), get_market_exchange_rate_callback]
    )
    return program


def clear_state_program():
    program = Seq(
        [
            Return(Int(1))
        ]
    )
    return program


if __name__ == "__main__":
    with open("algorand_oracle_callback_approval.teal", "w") as f:
        compiled = compileTeal(approval_program(), mode=Mode.Application, version=5)
        f.write(compiled)

    with open("algorand_oracle_callback_clear_state.teal", "w") as f:
        compiled = compileTeal(clear_state_program(), mode=Mode.Application, version=5)
        f.write(compiled)

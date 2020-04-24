package dk.cachet.rad.examples

import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.ProtocolVersion
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolRepository

class StudyProtocolRepositoryMock : StudyProtocolRepository {
    override fun add(protocol: StudyProtocol, versionTag: String) {
        TODO("Not yet implemented")
    }

    override fun getAllFor(owner: ProtocolOwner): Sequence<StudyProtocol> {
        TODO("Not yet implemented")
    }

    override fun getBy(owner: ProtocolOwner, protocolName: String, versionTag: String?): StudyProtocol {
        TODO("Not yet implemented")
    }

    override fun getVersionHistoryFor(owner: ProtocolOwner, protocolName: String): List<ProtocolVersion> {
        TODO("Not yet implemented")
    }

    override fun update(protocol: StudyProtocol, versionTag: String) {
        TODO("Not yet implemented")
    }
}